package com.sistema.sistema.infrastructure.persistence.order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
interface JpaOrderItemIncidentRepository extends JpaRepository<OrderItemIncidentEntity, Long> {
 List<OrderItemIncidentEntity> findByOrderIdOrderByReportedAtDescIdDesc(Long orderId);
 @Modifying
 @Query("UPDATE OrderItemIncidentEntity incident SET incident.status = 'SUPERSEDED' "
         + "WHERE incident.orderId = :orderId AND incident.orderItemId = :orderItemId "
         + "AND incident.resolution = 'DISCOUNT' AND incident.status IN ('APPROVED', 'APROVED')")
 void supersedeApprovedDiscounts(@Param("orderId") Long orderId, @Param("orderItemId") Long orderItemId);
}
