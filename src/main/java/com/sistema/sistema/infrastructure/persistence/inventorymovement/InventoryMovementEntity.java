package com.sistema.sistema.infrastructure.persistence.inventorymovement;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_movements_seq")
    @SequenceGenerator(name = "inventory_movements_seq", sequenceName = "inventory_movements_seq", allocationSize = 1)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "previous_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousStock;

    @Column(name = "current_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentStock;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {createdAt = LocalDateTime.now();}

    @PreUpdate
    protected void onUpdate() {modifiedAt = LocalDateTime.now();}
}