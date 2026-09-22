package com.sistema.sistema.infrastructure.persistence.inventorycount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_count_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCountSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_count_sessions_seq")
    @SequenceGenerator(
            name = "inventory_count_sessions_seq",
            sequenceName = "inventory_count_sessions_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "count_number", unique = true, length = 30)
    private String countNumber;

    @Column(name = "business_date")
    private LocalDate businessDate;

    private String status;

    @Column(name = "opened_by")
    private Long openedBy;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "total_products")
    private Long totalProducts;

    @Column(name = "counted_products")
    private Long countedProducts;

    @Column(name = "matched_products")
    private Long matchedProducts;

    @Column(name = "shortage_products")
    private Long shortageProducts;

    @Column(name = "surplus_products")
    private Long surplusProducts;

    @Column(name = "opening_comment")
    private String openingComment;

    @Column(name = "closing_comment")
    private String closingComment;
}
