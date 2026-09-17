package com.sistema.sistema.infrastructure.persistence.catalogconfig;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "catalog_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalog_configs_seq")
    @SequenceGenerator(
            name = "catalog_configs_seq",
            sequenceName = "catalog_configs_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // PORTADA

    @Column(name = "show_cover")
    private Boolean showCover = true;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "header_image", length = 500)
    private String headerImage;

    // VISTA PRODUCTOS

    @Column(name = "view_mode", length = 20)
    private String viewMode = "GRID";

    @Column(name = "columns")
    private Integer columns = 3;

    @Column(name = "show_image")
    private Boolean showImage = true;

    @Column(name = "show_price")
    private Boolean showPrice = true;

    @Column(name = "show_price_promo")
    private Boolean showPricePromo = false;

    @Column(name = "show_description")
    private Boolean showDescription = true;

    @Column(name = "show_code")
    private Boolean showCode = true;

    @Column(name = "card_border")
    private Boolean cardBorder = false;

    @Column(name = "show_status")
    private Boolean showStatus = true;

    // PAGINACIÓN

    @Column(name = "products_per_page")
    private Integer productsPerPage = 12;

    @Column(name = "show_page_number")
    private Boolean showPageNumber = true;

    @Column(name = "show_header")
    private Boolean showHeader = true;

    @Column(name = "show_footer")
    private Boolean showFooter = true;

    // ESTADO

    @Column(name = "active")
    private Boolean active = true;

    // AUDITORÍA

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}