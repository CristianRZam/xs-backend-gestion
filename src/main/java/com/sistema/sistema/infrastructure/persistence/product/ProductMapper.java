package com.sistema.sistema.infrastructure.persistence.product;

import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.domain.model.Product;
import com.sistema.sistema.infrastructure.persistence.parameter.JpaParameterRepository;
import com.sistema.sistema.infrastructure.persistence.parameter.ParameterEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final JpaParameterRepository jpaParameterRepository;

    public ProductMapper(JpaParameterRepository jpaParameterRepository) {
        this.jpaParameterRepository = jpaParameterRepository;
    }

    public Product toDomain(ProductEntity entity) {
        if (entity == null) return null;

        return Product.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .categoryId(entity.getCategoryId())
                .unitMeasureId(entity.getUnitMeasureId())
                .valuationMethodId(entity.getValuationMethodId())
                .manageVariants(entity.getManageVariants())
                .basePrice(entity.getBasePrice())
                .promoPrice(entity.getPromoPrice())
                .baseCost(entity.getBaseCost())
                .totalStock(entity.getTotalStock())
                .active(entity.getActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .modifiedAt(entity.getModifiedAt())
                .deletedBy(entity.getDeletedBy())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    public ProductDTO toDTOFromEntity(ProductEntity entity) {
        if (entity == null) return null;

        ParameterEntity categoryParam = jpaParameterRepository
                .findByParameterIdAndCode(entity.getCategoryId(), "CATEGORIA_PRODUCTO")
                .orElse(null);

        ParameterEntity unitParam = jpaParameterRepository
                .findByParameterIdAndCode(entity.getUnitMeasureId(), "UNIDAD_MEDIDA_PRODUCTO")
                .orElse(null);

        ParameterEntity valuationParam = jpaParameterRepository
                .findByParameterIdAndCode(entity.getUnitMeasureId(), "METODO_VALUACION")
                .orElse(null);

        return ProductDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .categoryId(entity.getCategoryId())
                .nameCategory(categoryParam != null ? categoryParam.getName() : null)
                .unitMeasureId(entity.getUnitMeasureId())
                .nameUnitMeasure(unitParam != null ? unitParam.getName() : null)
                .valuationMethodId(entity.getValuationMethodId())
                .nameValuationMethod(valuationParam != null ? valuationParam.getName() : null)
                .manageVariants(entity.getManageVariants())
                .basePrice(entity.getBasePrice())
                .promoPrice(entity.getPromoPrice())
                .baseCost(entity.getBaseCost())
                .totalStock(entity.getTotalStock())
                .active(entity.getActive())
                .deleted(entity.getDeletedAt() != null)
                .build();
    }

}
