package com.sistema.sistema.infrastructure.persistence.sale;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "sales") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleEntity {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_seq_generator")
    @SequenceGenerator(name = "sales_seq_generator", sequenceName = "sales_seq", allocationSize = 1)
    private Long id;
    @Column(name = "sale_number", nullable = false, unique = true, length = 30) private String saleNumber;
    @Column(name = "order_id") private Long orderId;
    @Column(name = "cash_register_id", nullable = false) private Long cashRegisterId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal discount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "deleted_by") private Long deletedBy;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<SaleItemEntity> items = new ArrayList<>();
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<PaymentEntity> payments = new ArrayList<>();
}
