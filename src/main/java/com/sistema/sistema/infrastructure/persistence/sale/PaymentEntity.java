package com.sistema.sistema.infrastructure.persistence.sale;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "payments") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentEntity {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payments_seq_generator")
    @SequenceGenerator(name = "payments_seq_generator", sequenceName = "payments_seq", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = false) private SaleEntity sale;
    @Column(name = "cash_session_id", nullable = false) private Long cashSessionId;
    @Column(name = "payment_method", nullable = false, length = 30) private String paymentMethod;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(name = "received_amount", precision = 12, scale = 2) private BigDecimal receivedAmount;
    @Column(name = "change_amount", precision = 12, scale = 2) private BigDecimal changeAmount;
    @Column(length = 100) private String reference;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
