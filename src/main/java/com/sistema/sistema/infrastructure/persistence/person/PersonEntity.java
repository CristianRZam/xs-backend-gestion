package com.sistema.sistema.infrastructure.persistence.person;

import com.sistema.sistema.infrastructure.persistence.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "persons_seq_gen")
    @SequenceGenerator(name = "persons_seq_gen", sequenceName = "persons_seq", allocationSize = 1)
    private Long id;

    @Column(name = "type_document")
    private Long typeDocument;

    @Column(length = 20)
    private String document;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String address;

    // Auditoría
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

    // Relación inversa: una persona puede tener múltiples cuentas de usuario
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserEntity> users;

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
