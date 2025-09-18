package com.sistema.sistema.infrastructure.persistence.parameter;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "parameters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parameters_seq_gen")
    @SequenceGenerator(name = "parameters_seq_gen", sequenceName = "parameters_seq", allocationSize = 1)
    private Long id;

    @Column(name = "parent_parameter_id")
    private Long parentParameterId;

    @Column(name = "parameter_id")
    private Long parameterId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "type")
    private Long type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(nullable = false)
    private Boolean active = true;

    // === Audit fields ===
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // === Callbacks de JPA ===
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
