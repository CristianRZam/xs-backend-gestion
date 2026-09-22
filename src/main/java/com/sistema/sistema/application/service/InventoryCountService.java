package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.inventorycount.InventoryCountCloseRequest;
import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.domain.usecase.InventoryCountUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.persistence.inventorycount.InventoryCountItemEntity;
import com.sistema.sistema.infrastructure.persistence.inventorycount.InventoryCountSessionEntity;
import com.sistema.sistema.infrastructure.persistence.inventorycount.JpaInventoryCountItemRepository;
import com.sistema.sistema.infrastructure.persistence.inventorycount.JpaInventoryCountSessionRepository;
import com.sistema.sistema.infrastructure.persistence.product.JpaProductRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryCountService implements InventoryCountUseCase {
    private static final String DOCUMENT_NUMBER_PREFIX = "CNT-";
    private static final DateTimeFormatter DOCUMENT_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final JpaInventoryCountSessionRepository sessions;
    private final JpaInventoryCountItemRepository items;
    private final JpaProductRepository products;
    private final InventoryMovementService movements;

    public InventoryCountService(
            JpaInventoryCountSessionRepository sessions,
            JpaInventoryCountItemRepository items,
            JpaProductRepository products,
            InventoryMovementService movements
    ) {
        this.sessions = sessions;
        this.items = items;
        this.products = products;
        this.movements = movements;
    }

    @Override
    @Transactional
    public InventoryCountSessionEntity open(String comment) {
        if (sessions.findFirstByBusinessDateAndStatusInOrderByIdDesc(
                LocalDate.now(), List.of("OPEN", "REVIEW")
        ).isPresent()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ya existe un conteo activo hoy.");
        }

        var productList = products.findByActiveTrueAndDeletedAtIsNullOrderByNameAsc();
        LocalDateTime openedAt = nextCountTime(LocalDateTime.now().withNano(0));
        var session = sessions.save(InventoryCountSessionEntity.builder()
                .countNumber(formatCountNumber(openedAt))
                .businessDate(LocalDate.now())
                .status("OPEN")
                .openedBy(SecurityUtil.getCurrentUserId())
                .openedAt(openedAt)
                .totalProducts((long) productList.size())
                .countedProducts(0L)
                .matchedProducts(0L)
                .shortageProducts(0L)
                .surplusProducts(0L)
                .openingComment(comment)
                .build());

        items.saveAll(productList.stream()
                .map(product -> InventoryCountItemEntity.builder()
                        .sessionId(session.getId())
                        .productId(product.getId())
                        // Audit value captured when the count starts.
                        .openingStock(product.getTotalStock() == null ? 0L : product.getTotalStock())
                        .resultStatus("PENDING")
                        .adjustmentStatus("NOT_REQUIRED")
                        .build())
                .toList());
        return session;
    }

    @Override
    public Map<String, Object> detail(Long id) {
        var session = findSession(id);
        var rows = items.findBySessionIdOrderById(id);
        return Map.of(
                "session", session,
                "items", rows.stream().map(item -> {
                    var product = products.findById(item.getProductId()).orElse(null);
                    return Map.of(
                            "item", item,
                            "productName", product == null ? "Producto eliminado" : product.getName(),
                            "productCode", product == null ? "-" : product.getCode(),
                            "currentStock", product == null || product.getTotalStock() == null
                                    ? 0L
                                    : product.getTotalStock()
                    );
                }).toList()
        );
    }

    @Override
    @Transactional
    public Map<String, Object> review(Long id, InventoryCountCloseRequest request) {
        var session = findOpenOrReviewSession(id);
        var submitted = validateCompleteCount(id, request);
        var totals = new Totals();

        for (var item : items.findBySessionIdOrderById(id)) {
            if (!"APPLIED".equals(item.getAdjustmentStatus())) {
                applyPhysicalCount(item, submitted.get(item.getProductId()));
                items.save(item);
            }
            totals.add(item);
        }

        updateReviewTotals(session, totals);
        return detail(id);
    }

    @Override
    @Transactional
    public InventoryCountSessionEntity close(Long id, InventoryCountCloseRequest request) {
        var session = findOpenOrReviewSession(id);
        var submitted = validateCompleteCount(id, request);
        var totals = new Totals();

        for (var item : items.findBySessionIdOrderById(id)) {
            if (!"APPLIED".equals(item.getAdjustmentStatus())) {
                var input = submitted.get(item.getProductId());
                applyPhysicalCount(item, input);
                if (item.getDifference() != 0 && Boolean.TRUE.equals(input.getApplyAdjustment())) {
                    applyAdjustment(id, item, input);
                }
                items.save(item);
            }
            totals.add(item);
        }

        session.setMatchedProducts(totals.matched);
        session.setShortageProducts(totals.shortages);
        session.setSurplusProducts(totals.surpluses);
        session.setCountedProducts(totals.counted());
        session.setClosedBy(SecurityUtil.getCurrentUserId());
        session.setClosedAt(LocalDateTime.now());
        session.setStatus("CLOSED");
        return sessions.save(session);
    }

    @Override
    public List<InventoryCountSessionEntity> history() {
        return sessions.findAllByOrderByOpenedAtDescIdDesc();
    }

    @Override
    public InventoryCountSessionEntity current() {
        return sessions.findFirstByBusinessDateAndStatusInOrderByIdDesc(
                LocalDate.now(), List.of("OPEN", "REVIEW")
        ).orElse(null);
    }

    private InventoryCountSessionEntity findSession(Long id) {
        return sessions.findById(id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Conteo no encontrado.")
        );
    }

    private InventoryCountSessionEntity findOpenOrReviewSession(Long id) {
        var session = findSession(id);
        if (!"OPEN".equals(session.getStatus()) && !"REVIEW".equals(session.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El conteo ya fue cerrado.");
        }
        return session;
    }

    private Map<Long, InventoryCountCloseRequest.Item> validateCompleteCount(
            Long sessionId,
            InventoryCountCloseRequest request
    ) {
        if (request == null || request.getItems() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe registrar el conteo físico de todos los productos.");
        }

        var expectedItems = items.findBySessionIdOrderById(sessionId);
        if (request.getItems().size() != expectedItems.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Debe registrar el conteo físico de todos los productos.");
        }

        var submitted = new HashMap<Long, InventoryCountCloseRequest.Item>();
        for (var input : request.getItems()) {
            if (input.getProductId() == null || input.getPhysicalStock() == null || input.getPhysicalStock() < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Conteo físico inválido.");
            }
            if (submitted.put(input.getProductId(), input) != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Un producto no puede contarse dos veces.");
            }
        }
        for (var expected : expectedItems) {
            if (!submitted.containsKey(expected.getProductId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "El conteo no incluye todos los productos de la sesión.");
            }
        }
        return submitted;
    }

    private void applyPhysicalCount(
            InventoryCountItemEntity item,
            InventoryCountCloseRequest.Item input
    ) {
        long expected = products.findById(item.getProductId())
                .map(product -> product.getTotalStock() == null ? 0L : product.getTotalStock())
                .orElse(0L);
        long difference = input.getPhysicalStock() - expected;

        item.setExpectedStock(expected);
        item.setPhysicalStock(input.getPhysicalStock());
        item.setDifference(difference);
        item.setCountedBy(SecurityUtil.getCurrentUserId());
        item.setCountedAt(LocalDateTime.now());
        item.setDifferenceReason(input.getReason());
        item.setDifferenceComment(input.getComment());

        if (difference == 0) {
            item.setResultStatus("MATCHED");
            item.setAdjustmentStatus("NOT_REQUIRED");
        } else if (difference < 0) {
            item.setResultStatus("SHORTAGE");
            item.setAdjustmentStatus("PENDING");
        } else {
            item.setResultStatus("SURPLUS");
            item.setAdjustmentStatus("PENDING");
        }
    }

    private void applyAdjustment(
            Long sessionId,
            InventoryCountItemEntity item,
            InventoryCountCloseRequest.Item input
    ) {
        if (input.getReason() == null || input.getReason().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Debe indicar el motivo para cada ajuste.");
        }
        var movement = new InventoryMovementCreateRequest();
        movement.setProductId(item.getProductId());
        movement.setType("ADJUSTMENT");
        movement.setQuantity(BigDecimal.valueOf(item.getPhysicalStock()));
        movement.setReason(input.getReason().trim());
        movement.setReferenceType("INVENTORY_COUNT");
        movement.setReferenceId(sessionId);

        var saved = movements.create(movement);
        item.setAdjustmentStatus("APPLIED");
        item.setAdjustmentMovementId(saved.getId());
        item.setApprovedBy(SecurityUtil.getCurrentUserId());
        item.setApprovedAt(LocalDateTime.now());
    }

    private void updateReviewTotals(InventoryCountSessionEntity session, Totals totals) {
        session.setCountedProducts(totals.counted());
        session.setMatchedProducts(totals.matched);
        session.setShortageProducts(totals.shortages);
        session.setSurplusProducts(totals.surpluses);
        session.setStatus("REVIEW");
        sessions.save(session);
    }

    private LocalDateTime nextCountTime(LocalDateTime now) {
        LocalDateTime candidateTime = now;
        do {
            String candidate = formatCountNumber(candidateTime);
            if (!sessions.existsByCountNumber(candidate)) {
                return candidateTime;
            }
            candidateTime = candidateTime.plusSeconds(1);
        } while (true);
    }

    private String formatCountNumber(LocalDateTime value) {
        return DOCUMENT_NUMBER_PREFIX + DOCUMENT_NUMBER_FORMAT.format(value);
    }

    private static final class Totals {
        private long matched;
        private long shortages;
        private long surpluses;

        private void add(InventoryCountItemEntity item) {
            if ("MATCHED".equals(item.getResultStatus())) {
                matched++;
            } else if ("SHORTAGE".equals(item.getResultStatus())) {
                shortages++;
            } else if ("SURPLUS".equals(item.getResultStatus())) {
                surpluses++;
            }
        }

        private long counted() {
            return matched + shortages + surpluses;
        }
    }
}
