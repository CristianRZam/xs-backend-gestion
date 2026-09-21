package com.sistema.sistema.infrastructure.persistence.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq_generator")
    @SequenceGenerator(name = "orders_seq_generator", sequenceName = "orders_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "cash_register_id")
    private Long cashRegisterId;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Column(name = "table_number", length = 20)
    private String tableNumber;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}