package com.sistema.sistema.infrastructure.persistence.dashboard;

import com.sistema.sistema.application.dto.response.dashboard.DailySalesDTO;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.application.dto.response.dashboard.DashboardSellerDTO;
import com.sistema.sistema.application.dto.response.dashboard.PaymentMethodDTO;
import com.sistema.sistema.application.dto.response.dashboard.ProductSalesDTO;
import com.sistema.sistema.domain.repository.DashboardRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class DashboardDAOImpl implements DashboardRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public DashboardDTO getSummary(LocalDate fromDate, LocalDate toDate, Long sellerId, String scope) {
        LocalDate exclusiveEnd = toDate.plusDays(1);
        log.debug("Generando dashboard {} entre {} y {} para vendedor {}", scope, fromDate, toDate, sellerId);

        Object[] totals = (Object[]) query("""
                SELECT COALESCE(SUM(s.total), 0), COUNT(*)
                FROM sales s
                WHERE s.status = 'COMPLETED' AND s.deleted_at IS NULL
                  AND s.created_at >= :fromDate AND s.created_at < :toDate
                """, sellerId, "s", fromDate, exclusiveEnd).getSingleResult();
        BigDecimal totalSales = number(totals[0]);
        long salesCount = number(totals[1]).longValue();
        BigDecimal average = salesCount == 0
                ? BigDecimal.ZERO
                : totalSales.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP);

        Long orders = number(query("""
                SELECT COUNT(*) FROM orders o
                WHERE o.deleted_at IS NULL
                  AND o.created_at >= :fromDate AND o.created_at < :toDate
                """, sellerId, "o", fromDate, exclusiveEnd).getSingleResult()).longValue();

        List<ProductSalesDTO> products = query("""
                SELECT si.product_id, p.name, SUM(si.quantity)
                FROM sale_items si
                JOIN sales s ON s.id = si.sale_id
                JOIN products p ON p.id = si.product_id
                WHERE s.status = 'COMPLETED' AND s.deleted_at IS NULL
                  AND s.created_at >= :fromDate AND s.created_at < :toDate
                """, sellerId, "s", fromDate, exclusiveEnd)
                .getResultList().stream().map(row -> {
                    Object[] values = (Object[]) row;
                    return ProductSalesDTO.builder()
                            .productId(number(values[0]).longValue())
                            .productName((String) values[1])
                            .quantity(number(values[2]).longValue())
                            .build();
                }).toList();

        // La cláusula ORDER/LIMIT se concatena después de aplicar el filtro común.
        products = products.stream()
                .sorted((a, b) -> Long.compare(b.getQuantity(), a.getQuantity()))
                .limit(10)
                .toList();

        List<PaymentMethodDTO> methods = query("""
                SELECT p.payment_method, SUM(p.amount)
                FROM payments p
                JOIN sales s ON s.id = p.sale_id
                WHERE s.status = 'COMPLETED' AND s.deleted_at IS NULL
                  AND s.created_at >= :fromDate AND s.created_at < :toDate
                """, sellerId, "s", fromDate, exclusiveEnd)
                .getResultList().stream().map(row -> {
                    Object[] values = (Object[]) row;
                    return PaymentMethodDTO.builder()
                            .method((String) values[0])
                            .total(number(values[1]))
                            .build();
                }).toList();
        methods = methods.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        PaymentMethodDTO::getMethod,
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, PaymentMethodDTO::getTotal, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> PaymentMethodDTO.builder().method(entry.getKey()).total(entry.getValue()).build())
                .toList();

        return DashboardDTO.builder()
                .scope(scope)
                .summaryDate(toDate)
                .fromDate(fromDate)
                .toDate(toDate)
                .sellerId(sellerId)
                .todaySales(totalSales)
                .todaySalesCount(salesCount)
                .averageSale(average)
                .todayOrders(orders)
                .weeklySales(dailySales(fromDate, toDate, sellerId))
                .topProducts(products)
                .paymentMethods(methods)
                .build();
    }

    @Override
    public List<DashboardSellerDTO> getActiveSellers() {
        return em.createNativeQuery("""
                SELECT DISTINCT u.id, p.full_name
                FROM users u JOIN persons p ON p.id = u.person_id
                WHERE u.active = true AND u.deleted_at IS NULL
                ORDER BY p.full_name
                """).getResultList().stream().map(row -> {
            Object[] values = (Object[]) row;
            return DashboardSellerDTO.builder()
                    .id(number(values[0]).longValue())
                    .name((String) values[1])
                    .build();
        }).toList();
    }

    private List<DailySalesDTO> dailySales(LocalDate from, LocalDate to, Long sellerId) {
        Query query = query("""
                SELECT DATE(s.created_at), SUM(s.total)
                FROM sales s
                WHERE s.status = 'COMPLETED' AND s.deleted_at IS NULL
                  AND s.created_at >= :fromDate AND s.created_at < :toDate
                """, sellerId, "s", from, to.plusDays(1));
        Map<LocalDate, BigDecimal> totals = new HashMap<>();
        query.getResultList().forEach(row -> {
            Object[] values = (Object[]) row;
            totals.merge(date(values[0]), number(values[1]), BigDecimal::add);
        });
        List<DailySalesDTO> result = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            result.add(DailySalesDTO.builder().date(date)
                    .total(totals.getOrDefault(date, BigDecimal.ZERO)).build());
        }
        return result;
    }

    private Query query(String baseSql, Long sellerId, String alias, LocalDate from, LocalDate to) {
        String sellerFilter = sellerId == null ? "" : " AND " + alias + ".created_by = :sellerId";
        Query query = em.createNativeQuery(baseSql + sellerFilter + groupByFor(baseSql));
        query.setParameter("fromDate", from);
        query.setParameter("toDate", to);
        if (sellerId != null) query.setParameter("sellerId", sellerId);
        return query;
    }

    private String groupByFor(String sql) {
        if (sql.contains("si.product_id")) return " GROUP BY si.product_id, p.name";
        if (sql.contains("p.payment_method")) return " GROUP BY p.payment_method";
        if (sql.contains("DATE(s.created_at)")) return " GROUP BY DATE(s.created_at)";
        return "";
    }

    private LocalDate date(Object value) {
        return value instanceof Date date ? date.toLocalDate() : LocalDate.parse(value.toString());
    }

    private BigDecimal number(Object value) {
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
    }
}
