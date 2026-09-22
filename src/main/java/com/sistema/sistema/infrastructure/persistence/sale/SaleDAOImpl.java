package com.sistema.sistema.infrastructure.persistence.sale;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.domain.repository.SaleRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;

@Repository
public class SaleDAOImpl implements SaleRepository {
    private static final String DOCUMENT_NUMBER_PREFIX = "VTA-";
    private static final DateTimeFormatter DOCUMENT_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JpaSaleRepository jpa;
    public SaleDAOImpl(JpaSaleRepository jpa) { this.jpa = jpa; }

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
            sale.getPayments().add(PaymentEntity.builder().sale(sale).cashSessionId(cashSessionId)
                    .paymentMethod(payment.getPaymentMethod().trim().toUpperCase())
                    .amount(payment.getAmount()).reference(payment.getReference())
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
    @Override public boolean existsByOrderId(Long orderId) {
        return jpa.existsByOrderIdAndDeletedAtIsNull(orderId);
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
        return SaleDTO.builder().id(sale.getId()).saleNumber(sale.getSaleNumber())
                .orderId(sale.getOrderId()).cashRegisterId(sale.getCashRegisterId())
                .subtotal(sale.getSubtotal()).discount(sale.getDiscount()).total(sale.getTotal())
                .status(sale.getStatus()).createdBy(sale.getCreatedBy())
                .createdByName(createdByName).createdAt(sale.getCreatedAt())
                .items(sale.getItems().stream().map(i -> SaleDTO.SaleItemDTO.builder()
                        .productId(i.getProductId()).productName(jpa.findProductName(i.getProductId()))
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                        .discount(i.getDiscount()).subtotal(i.getSubtotal()).build()).toList())
                .payments(sale.getPayments().stream().map(p -> SaleDTO.PaymentDTO.builder()
                        .paymentMethod(p.getPaymentMethod()).amount(p.getAmount()).reference(p.getReference()).build()).toList())
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
