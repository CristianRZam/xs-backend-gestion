package com.sistema.sistema.infrastructure.persistence.district;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "districts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "districts_seq")
    @SequenceGenerator(name = "districts_seq", sequenceName = "districts_seq", allocationSize = 1)
    private Long id;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 150, nullable = false)
    private String province;

    @Column(length = 150, nullable = false)
    private String department;

    @Column
    private Boolean active = true;

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

}
