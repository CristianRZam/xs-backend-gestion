package com.sistema.sistema.infrastructure.persistence.birthrecord;

import com.sistema.sistema.infrastructure.persistence.district.DistrictEntity;
import com.sistema.sistema.infrastructure.persistence.parent.ParentEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "birth_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "birth_records_seq")
    @SequenceGenerator(name = "birth_records_seq", sequenceName = "birth_records_seq", allocationSize = 1)
    private Long id;

    // ============================
    // Datos generales
    // ============================

    @Column(nullable = false)
    private Integer year;

    @Column(name = "act_number", length = 50, nullable = false)
    private String actNumber;

    @Column(name = "person_name", length = 255, nullable = false)
    private String personName;

    @Column(length = 20)
    private String sex;

    @Column(name = "birth_place", length = 255)
    private String birthPlace;

    @Column(name = "birth_datetime")
    private LocalDateTime birthDatetime;

    // ============================
    // Localidad
    // ============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private DistrictEntity district;

    // ============================
    // Padres
    // ============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    private ParentEntity father;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    private ParentEntity mother;

    // ============================
    // Declarante
    // ============================

    @Column(name = "declarant_name", length = 255)
    private String declarantName;

    @Column(name = "declarant_document", length = 50)
    private String declarantDocument;

    // ============================
    // Datos de expedición
    // ============================

    @Column(name = "record_place", length = 255)
    private String recordPlace;

    @Column(name = "record_datetime")
    private LocalDateTime recordDatetime;

    // ============================
    // Auditoría
    // ============================

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
