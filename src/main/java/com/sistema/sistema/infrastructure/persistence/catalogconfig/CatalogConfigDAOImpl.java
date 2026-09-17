package com.sistema.sistema.infrastructure.persistence.catalogconfig;

import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigCreateRequest;
import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigUpdateRequest;
import com.sistema.sistema.application.dto.response.catalogconfig.CatalogConfigDTO;
import com.sistema.sistema.domain.repository.CatalogConfigRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CatalogConfigDAOImpl implements CatalogConfigRepository {

    private final JpaCatalogConfigRepository jpa;
    private final CatalogConfigMapper mapper;

    public CatalogConfigDAOImpl( JpaCatalogConfigRepository jpa, CatalogConfigMapper mapper ) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<CatalogConfigDTO> findAll() {
        return jpa.findByDeletedAtIsNull()
                .stream()
                .map(mapper::toDTOFromEntity)
                .toList();
    }

    @Override
    public CatalogConfigDTO findById(Long id) {
        CatalogConfigEntity entity = jpa.findById(id).orElse(null);
        return entity == null ? null : mapper.toDTOFromEntity(entity);
    }

    @Override
    public CatalogConfigDTO create(CatalogConfigCreateRequest request) {

        CatalogConfigEntity entity = new CatalogConfigEntity();

        entity.setName(request.getName());

        // PORTADA
        entity.setShowCover(request.getShowCover());
        entity.setCoverImage(request.getCoverImage());
        entity.setHeaderImage(request.getHeaderImage());

        // VISTA PRODUCTOS
        entity.setViewMode(request.getViewMode());
        entity.setColumns(request.getColumns());

        entity.setShowImage(request.getShowImage());
        entity.setShowPrice(request.getShowPrice());
        entity.setShowPricePromo(request.getShowPricePromo());
        entity.setShowDescription(request.getShowDescription());
        entity.setShowCode(request.getShowCode());
        entity.setCardBorder(request.getCardBorder());
        entity.setShowStatus(request.getShowStatus());

        // PAGINACIÓN
        entity.setProductsPerPage(request.getProductsPerPage());
        entity.setShowPageNumber(request.getShowPageNumber());
        entity.setShowHeader(request.getShowHeader());
        entity.setShowFooter(request.getShowFooter());

        entity.setActive(true);

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setCreatedBy(currentUserId);

        CatalogConfigEntity saved = jpa.save(entity);

        return mapper.toDTOFromEntity(saved);
    }

    @Override
    public CatalogConfigDTO update(CatalogConfigUpdateRequest request) {

        CatalogConfigEntity entity = jpa.findById(request.getId())
                .orElseThrow(() ->
                        new RuntimeException("Configuración no encontrada con id: " + request.getId()));

        entity.setName(request.getName());

        // PORTADA
        entity.setShowCover(request.getShowCover());
        entity.setCoverImage(request.getCoverImage());
        entity.setHeaderImage(request.getHeaderImage());

        // VISTA PRODUCTOS
        entity.setViewMode(request.getViewMode());
        entity.setColumns(request.getColumns());

        entity.setShowImage(request.getShowImage());
        entity.setShowPrice(request.getShowPrice());
        entity.setShowPricePromo(request.getShowPricePromo());
        entity.setShowDescription(request.getShowDescription());
        entity.setShowCode(request.getShowCode());
        entity.setCardBorder(request.getCardBorder());
        entity.setShowStatus(request.getShowStatus());

        // PAGINACIÓN
        entity.setProductsPerPage(request.getProductsPerPage());
        entity.setShowPageNumber(request.getShowPageNumber());
        entity.setShowHeader(request.getShowHeader());
        entity.setShowFooter(request.getShowFooter());

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(LocalDateTime.now());

        CatalogConfigEntity saved = jpa.save(entity);

        return mapper.toDTOFromEntity(saved);
    }

    @Override
    public boolean delete(Long id) {

        CatalogConfigEntity entity = jpa.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Configuración no encontrada con id: " + id));

        Long currentUserId = SecurityUtil.getCurrentUserId();

        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(currentUserId);

        jpa.save(entity);

        return true;
    }

}