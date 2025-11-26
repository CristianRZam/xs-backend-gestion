package com.sistema.sistema.infrastructure.persistence.parent;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "parents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parents_seq")
    @SequenceGenerator(name = "parents_seq", sequenceName = "parents_seq", allocationSize = 1)
    private Long id;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "natural_of", length = 255)
    private String naturalOf;

    @Column(length = 100)
    private String nationality;

    @Column(length = 255)
    private String occupation;

    @Column(length = 255)
    private String address;

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
