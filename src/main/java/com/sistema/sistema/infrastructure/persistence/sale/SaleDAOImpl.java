package com.sistema.sistema.infrastructure.persistence.sale;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.domain.repository.SaleRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;

@Repository
public class SaleDAOImpl implements SaleRepository {
    private static final String DOCUMENT_NUMBER_PREFIX = "VTA-";
    private static final DateTimeFormatter DOCUMENT_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JpaSaleRepository jpa;
    private final JpaSaleCancellationRepository cancellationJpa;

    public SaleDAOImpl(JpaSaleRepository jpa, JpaSaleCancellationRepository cancellationJpa) {
        this.jpa = jpa;
        this.cancellationJpa = cancellationJpa;
    }

    @Override
    public SaleDTO create(SaleCreateRequest request, Long cashRegisterId, Long cashSessionId,
                          BigDecimal subtotal, BigDecimal total) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = nextSaleTime(LocalDateTime.now().withNano(0));
        SaleEntity sale = SaleEntity.builder()
                .saleNumber(formatSaleNumber(now)).orderId(request.getOrderId())
                .cashRegisterId(cashRegisterId).subtotal(subtotal)
                .discount(zero(request.getDiscount())).total(total).status("COMPLETED")
                .createdBy(userId).createdAt(now).build();
        for (SaleCreateRequest.SaleItemRequest item : request.getItems()) {
            BigDecimal itemSubtotal = item.getUnitPrice().multiply(item.getQuantity())
                    .subtract(zero(item.getDiscount()));
            sale.getItems().add(SaleItemEntity.builder().sale(sale).productId(item.getProductId())
                    .quantity(item.getQuantity()).unitPrice(item.getUnitPrice())
                    .discount(zero(item.getDiscount())).subtotal(itemSubtotal).build());
        }
        for (SaleCreateRequest.PaymentRequest payment : request.getPayments()) {
            String paymentMethod = payment.getPaymentMethod().trim().toUpperCase(Locale.ROOT);
            BigDecimal receivedAmount = payment.getReceivedAmount();
            BigDecimal changeAmount = "CASH".equals(paymentMethod)
                    ? receivedAmount.subtract(payment.getAmount())
                    : null;
            sale.getPayments().add(PaymentEntity.builder().sale(sale).cashSessionId(cashSessionId)
                    .paymentMethod(paymentMethod).amount(payment.getAmount())
                    .receivedAmount(receivedAmount).changeAmount(changeAmount)
                    .reference(payment.getReference())
                    .createdBy(userId).createdAt(now).build());
        }
        return toDto(jpa.save(sale));
    }

    @Override public SaleDTO getById(Long id) {
        return toDto(jpa.findByIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new EntityNotFoundException("Venta no encontrada con id: " + id)));
    }
    @Override public List<SaleDTO> getAll() {
        return jpa.findByDeletedAtIsNullOrderByCreatedAtDescIdDesc().stream()
                .map(this::toDto)
                .toList();
    }
    @Override public PageResponseDTO<SaleDTO> getPage(int page, int size, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime to = toDate == null ? null : toDate.plusDays(1).atStartOfDay();
        PageRequest pageable = PageRequest.of(page, size);
        Page<SaleEntity> result = from == null && to == null ? jpa.findByDeletedAtIsNullOrderByCreatedAtDescIdDesc(pageable) : from != null && to != null ? jpa.findByDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(from, to, pageable) : from != null ? jpa.findByDeletedAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtDescIdDesc(from, pageable) : jpa.findByDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(to, pageable);
        return new PageResponseDTO<>(result.getContent().stream().map(this::toDto).toList(), result.getTotalElements(), page, size, result.hasNext());
    }
    @Override public boolean existsByOrderId(Long orderId) {
        return jpa.existsByOrderIdAndDeletedAtIsNullAndStatus(orderId, "COMPLETED");
    }

    @Override
    public boolean isOriginalCashSessionOpen(Long saleId) {
        return jpa.isOriginalCashSessionOpen(saleId);
    }

    @Override
    public SaleDTO cancel(Long saleId, String reason) {
        SaleEntity sale = jpa.findByIdAndDeletedAtIsNull(saleId).orElseThrow(
                () -> new EntityNotFoundException("Venta no encontrada con id: " + saleId));
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        sale.setStatus("CANCELLED");
        sale.setModifiedBy(userId);
        sale.setModifiedAt(now);
        jpa.save(sale);
        cancellationJpa.save(SaleCancellationEntity.builder()
                .saleId(saleId)
                .reason(reason)
                .cancelledBy(userId)
                .cancelledAt(now)
                .build());
        return toDto(sale);
    }

    @Override
    public CashSessionSalesSummaryDTO getCashSessionSummary(Long cashSessionId) {
        List<SaleDTO> sales = jpa.findCompletedByCashSessionId(cashSessionId).stream()
                .map(this::toDto).toList();
        BigDecimal totalSold = sales.stream().map(SaleDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totals = new LinkedHashMap<String, BigDecimal>();
        sales.stream().flatMap(sale -> sale.getPayments().stream()).forEach(payment ->
                totals.merge(payment.getPaymentMethod(), payment.getAmount(), BigDecimal::add)
        );
        return CashSessionSalesSummaryDTO.builder()
                .cashSessionId(cashSessionId).totalSold(totalSold).sales(sales)
                .paymentMethods(totals.entrySet().stream().map(entry ->
                        CashSessionSalesSummaryDTO.PaymentMethodTotalDTO.builder()
                                .paymentMethod(entry.getKey()).total(entry.getValue()).build()
                ).toList()).build();
    }

    private SaleDTO toDto(SaleEntity sale) {
        String createdByName = jpa.findCreatedByName(sale.getId())
                .map(SaleUserNameProjection::getCreatedByName)
                .orElse(null);
        SaleCancellationEntity cancellation = cancellationJpa.findBySaleId(sale.getId()).orElse(null);
        return SaleDTO.builder().id(sale.getId()).saleNumber(sale.getSaleNumber())
                .orderId(sale.getOrderId()).cashRegisterId(sale.getCashRegisterId())
                .subtotal(sale.getSubtotal()).discount(sale.getDiscount()).total(sale.getTotal())
                .status(sale.getStatus()).createdBy(sale.getCreatedBy())
                .createdByName(createdByName).createdAt(sale.getCreatedAt())
                .cancellationReason(cancellation == null ? null : cancellation.getReason())
                .cancelledBy(cancellation == null ? null : cancellation.getCancelledBy())
                .cancelledByName(cancellation == null ? null : cancellationJpa.findCancelledByName(sale.getId()).orElse(null))
                .cancelledAt(cancellation == null ? null : cancellation.getCancelledAt())
                .items(sale.getItems().stream().map(i -> SaleDTO.SaleItemDTO.builder()
                        .productId(i.getProductId()).productName(jpa.findProductName(i.getProductId()))
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                        .discount(i.getDiscount()).subtotal(i.getSubtotal()).build()).toList())
                .payments(sale.getPayments().stream().map(p -> SaleDTO.PaymentDTO.builder()
                        .paymentMethod(p.getPaymentMethod()).amount(p.getAmount())
                        .receivedAmount(p.getReceivedAmount()).changeAmount(p.getChangeAmount())
                        .reference(p.getReference()).build()).toList())
                .build();
    }
    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private LocalDateTime nextSaleTime(LocalDateTime now) {
        LocalDateTime candidateTime = now;
        do {
            String candidate = formatSaleNumber(candidateTime);
            if (!jpa.existsBySaleNumber(candidate)) {
                return candidateTime;
            }
            candidateTime = candidateTime.plusSeconds(1);
        } while (true);
    }

    private String formatSaleNumber(LocalDateTime value) {
        return DOCUMENT_NUMBER_PREFIX + DOCUMENT_NUMBER_FORMAT.format(value);
    }
}
