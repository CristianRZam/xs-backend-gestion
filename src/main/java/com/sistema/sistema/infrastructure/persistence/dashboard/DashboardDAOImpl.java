package com.sistema.sistema.infrastructure.persistence.dashboard;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.domain.repository.DashboardRepository;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
@Repository public class DashboardDAOImpl implements DashboardRepository {
 @PersistenceContext private EntityManager em;

    @Override
    public DashboardDTO getPersonalSummary(Long userId, LocalDate day) {
        Objects.requireNonNull(userId, "Personal dashboard requires a user");
        Objects.requireNonNull(day, "Personal dashboard requires a date");

        Object[] totals = (Object[]) personalQuery("""
                SELECT COALESCE(SUM(s.total), 0), COUNT(*)
                FROM sales s
                WHERE s.created_by = :userId
                  AND s.created_at >= :start AND s.created_at < :end
                  AND s.status = 'COMPLETED' AND s.deleted_at IS NULL
                """, userId, day).getSingleResult();
        BigDecimal total = n(totals[0]);
        long count = n(totals[1]).longValue();

        long orders = n(personalQuery("""
                SELECT COUNT(*) FROM orders o
                WHERE o.created_by = :userId
                  AND o.created_at >= :start AND o.created_at < :end
                  AND o.deleted_at IS NULL
                """, userId, day).getSingleResult()).longValue();

        List<?> productRows = personalQuery("""
                SELECT si.product_id, p.name, SUM(si.quantity)
                FROM sale_items si
                JOIN sales s ON s.id = si.sale_id
                JOIN products p ON p.id = si.product_id
                WHERE s.created_by = :userId
                  AND s.created_at >= :start AND s.created_at < :end
                  AND s.status = 'COMPLETED' AND s.deleted_at IS NULL
                GROUP BY si.product_id, p.name
                ORDER BY SUM(si.quantity) DESC, si.product_id
                LIMIT 5
                """, userId, day).getResultList();
        List<DashboardDTO.ProductSalesDTO> products = productRows.stream()
                .map(row -> {
                    Object[] values = (Object[]) row;
                    return DashboardDTO.ProductSalesDTO.builder()
                            .productId(n(values[0]).longValue())
                            .productName((String) values[1])
                            .quantity(n(values[2]).longValue())
                            .build();
                })
                .toList();

        List<?> paymentRows = personalQuery("""
                SELECT p.payment_method, SUM(p.amount)
                FROM payments p JOIN sales s ON s.id = p.sale_id
                WHERE s.created_by = :userId
                  AND s.created_at >= :start AND s.created_at < :end
                  AND s.status = 'COMPLETED' AND s.deleted_at IS NULL
                GROUP BY p.payment_method
                ORDER BY SUM(p.amount) DESC, p.payment_method
                """, userId, day).getResultList();
        List<DashboardDTO.PaymentMethodDTO> methods = paymentRows.stream()
                .map(row -> {
                    Object[] values = (Object[]) row;
                    return DashboardDTO.PaymentMethodDTO.builder()
                            .method((String) values[0])
                            .total(n(values[1]))
                            .build();
                })
                .toList();

        BigDecimal average = count == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
        return DashboardDTO.builder()
                .scope("PERSONAL")
                .summaryDate(day)
                .todaySales(total)
                .todaySalesCount(count)
                .averageSale(average)
                .todayOrders(orders)
                .weeklySales(List.of())
                .topProducts(products)
                .paymentMethods(methods)
                .build();
    }

    private Query personalQuery(String sql, Long userId, LocalDate day) {
        return em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("start", day.atStartOfDay())
                .setParameter("end", day.plusDays(1).atStartOfDay());
    }

 public DashboardDTO getSummary(){
  Object[] todayTotals = (Object[]) em.createNativeQuery("SELECT COALESCE(SUM(total),0), COUNT(*) FROM sales WHERE status='COMPLETED' AND deleted_at IS NULL AND DATE(created_at)=CURRENT_DATE").getSingleResult();
  BigDecimal today=n(todayTotals[0]);
  long salesCount=n(todayTotals[1]).longValue();
  BigDecimal average = salesCount == 0 ? BigDecimal.ZERO : today.divide(BigDecimal.valueOf(salesCount), 2, java.math.RoundingMode.HALF_UP);
  Long orders=n(em.createNativeQuery("SELECT COUNT(*) FROM orders WHERE deleted_at IS NULL AND DATE(created_at)=CURRENT_DATE").getSingleResult()).longValue();
  List<DashboardDTO.ProductSalesDTO> products=em.createNativeQuery("SELECT si.product_id,p.name,SUM(si.quantity) FROM sale_items si JOIN sales s ON s.id=si.sale_id JOIN products p ON p.id=si.product_id WHERE s.status='COMPLETED' AND s.deleted_at IS NULL GROUP BY si.product_id,p.name ORDER BY SUM(si.quantity) DESC LIMIT 10").getResultList().stream().map(r->{Object[]v=(Object[])r;return DashboardDTO.ProductSalesDTO.builder().productId(n(v[0]).longValue()).productName((String)v[1]).quantity(n(v[2]).longValue()).build();}).toList();
  List<DashboardDTO.PaymentMethodDTO> methods=em.createNativeQuery("SELECT p.payment_method,SUM(p.amount) FROM payments p JOIN sales s ON s.id=p.sale_id WHERE s.status='COMPLETED' AND s.deleted_at IS NULL GROUP BY p.payment_method ORDER BY SUM(p.amount) DESC").getResultList().stream().map(r->{Object[]v=(Object[])r;return DashboardDTO.PaymentMethodDTO.builder().method((String)v[0]).total(n(v[1])).build();}).toList();
  return DashboardDTO.builder().todaySales(today).todaySalesCount(salesCount).averageSale(average).todayOrders(orders).weeklySales(week()).topProducts(products).paymentMethods(methods).build(); }
 private List<DashboardDTO.DailySalesDTO> week(){Map<LocalDate,BigDecimal>m=new HashMap<>();em.createNativeQuery("SELECT DATE(created_at),SUM(total) FROM sales WHERE status='COMPLETED' AND deleted_at IS NULL AND DATE(created_at) BETWEEN CURRENT_DATE-6 AND CURRENT_DATE GROUP BY DATE(created_at)").getResultList().forEach(r->{Object[]v=(Object[])r;m.put(date(v[0]),n(v[1]));});List<DashboardDTO.DailySalesDTO>x=new ArrayList<>();for(int i=6;i>=0;i--){LocalDate d=LocalDate.now().minusDays(i);x.add(DashboardDTO.DailySalesDTO.builder().date(d).total(m.getOrDefault(d,BigDecimal.ZERO)).build());}return x;}
 private LocalDate date(Object value){return value instanceof Date d?d.toLocalDate():value instanceof LocalDate d?d:LocalDate.parse(value.toString());}
 private BigDecimal n(Object v){return v instanceof BigDecimal b?b:new BigDecimal(v.toString());}
}
