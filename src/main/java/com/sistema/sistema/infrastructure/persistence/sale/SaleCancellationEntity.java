package com.sistema.sistema.infrastructure.persistence.sale;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sale_cancellations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCancellationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sale_cancellations_seq_generator")
    @SequenceGenerator(name = "sale_cancellations_seq_generator", sequenceName = "sale_cancellations_seq", allocationSize = 1)
    private Long id;

    @Column(name = "sale_id", nullable = false, unique = true)
    private Long saleId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "cancelled_by", nullable = false)
    private Long cancelledBy;

    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;
}
