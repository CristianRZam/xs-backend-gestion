package com.sistema.sistema.infrastructure.persistence.sale;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "sale_items") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sale_items_seq_generator")
    @SequenceGenerator(name = "sale_items_seq_generator", sequenceName = "sale_items_seq", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = false) private SaleEntity sale;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal quantity;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal discount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
}
