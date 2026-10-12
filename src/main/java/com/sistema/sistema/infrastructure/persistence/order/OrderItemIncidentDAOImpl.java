package com.sistema.sistema.infrastructure.persistence.order;
import com.sistema.sistema.domain.model.OrderItemIncident;
import com.sistema.sistema.domain.repository.OrderItemIncidentRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class OrderItemIncidentDAOImpl implements OrderItemIncidentRepository {
 private final JpaOrderItemIncidentRepository jpa;
 public OrderItemIncidentDAOImpl(JpaOrderItemIncidentRepository jpa) { this.jpa = jpa; }
 public OrderItemIncident save(OrderItemIncident value) { return toDomain(jpa.save(toEntity(value))); }
 public List<OrderItemIncident> findByOrderId(Long id) { return jpa.findByOrderIdOrderByReportedAtDescIdDesc(id).stream().map(this::toDomain).toList(); }
 public void supersedeApprovedDiscounts(Long orderId, Long orderItemId) { jpa.supersedeApprovedDiscounts(orderId, orderItemId); }
 private OrderItemIncidentEntity toEntity(OrderItemIncident v) { return OrderItemIncidentEntity.builder().id(v.getId()).orderId(v.getOrderId()).orderItemId(v.getOrderItemId()).productId(v.getProductId()).resolution(v.getResolution()).reason(v.getReason()).discountAmount(v.getDiscountAmount()).reportedBy(v.getReportedBy()).reportedAt(v.getReportedAt()).status(v.getStatus()).approvedBy(v.getApprovedBy()).approvedAt(v.getApprovedAt()).build(); }
 private OrderItemIncident toDomain(OrderItemIncidentEntity v) { return OrderItemIncident.builder().id(v.getId()).orderId(v.getOrderId()).orderItemId(v.getOrderItemId()).productId(v.getProductId()).resolution(v.getResolution()).reason(v.getReason()).discountAmount(v.getDiscountAmount()).reportedBy(v.getReportedBy()).reportedAt(v.getReportedAt()).status(v.getStatus()).approvedBy(v.getApprovedBy()).approvedAt(v.getApprovedAt()).build(); }
}
