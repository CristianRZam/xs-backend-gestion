package com.sistema.sistema.infrastructure.persistence.productimage;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_images_seq_gen")
    @SequenceGenerator(name = "product_images_seq_gen", sequenceName = "product_images_seq", allocationSize = 1)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "is_main")
    private Boolean isMain = false;

    @Column(name = "order_number")
    private Integer orderNumber = 1;

    @Column(name = "active")
    private Boolean active = true;

    // ===============================
    // Auditoría
    // ===============================
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
