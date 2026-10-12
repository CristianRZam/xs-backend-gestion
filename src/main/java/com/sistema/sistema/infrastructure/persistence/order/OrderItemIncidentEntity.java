package com.sistema.sistema.infrastructure.persistence.order;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "order_item_incidents") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemIncidentEntity {
 @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_incidents_seq_generator")
 @SequenceGenerator(name = "order_item_incidents_seq_generator", sequenceName = "order_item_incidents_seq", allocationSize = 1) private Long id;
 @Column(name="order_id", nullable=false) private Long orderId;
 @Column(name="order_item_id", nullable=false) private Long orderItemId;
 @Column(name="product_id", nullable=false) private Long productId;
 @Column(nullable=false, length=30) private String resolution;
 @Column(nullable=false, length=500) private String reason;
 @Column(name="discount_amount", nullable=false, precision=12, scale=2) private BigDecimal discountAmount;
 @Column(name="reported_by", nullable=false) private Long reportedBy;
 @Column(name="reported_at", nullable=false) private LocalDateTime reportedAt;
 @Column(nullable=false, length=20) private String status;
 @Column(name="approved_by", nullable=false) private Long approvedBy;
 @Column(name="approved_at", nullable=false) private LocalDateTime approvedAt;
}
