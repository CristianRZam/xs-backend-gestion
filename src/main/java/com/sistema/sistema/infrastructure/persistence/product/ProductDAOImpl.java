package com.sistema.sistema.infrastructure.persistence.product;

import com.sistema.sistema.application.dto.request.product.ProductCreateRequest;
import com.sistema.sistema.application.dto.request.product.ProductUpdateRequest;
import com.sistema.sistema.application.dto.request.product.ProductViewRequest;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.product.ProductViewResponse;
import com.sistema.sistema.domain.model.Product;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductDAOImpl implements ProductRepository {

    private final JpaProductRepository jpa;
    private final ProductMapper mapper;

    public ProductDAOImpl(JpaProductRepository jpa, ProductMapper mapper) {
        this.jpa= jpa;
        this.mapper = mapper;
    }


    @Override
    public List<ProductDTO> findAllFiltered(ProductViewRequest request) {
        List<Integer> categories = (request.getCategories() == null || request.getCategories().isEmpty())
                ? null
                : request.getCategories();

        List<Integer> unitMeasures = (request.getUnitMeasures() == null || request.getUnitMeasures().isEmpty())
                ? null
                : request.getUnitMeasures();

        List<Integer> valuationMethods = (request.getValuationMethods() == null || request.getValuationMethods().isEmpty())
                ? null
                : request.getValuationMethods();

        Long minStock = request.getMinimumStock() != null ? request.getMinimumStock().longValue() : null;
        Long maxStock = request.getMaximumStock() != null ? request.getMaximumStock().longValue() : null;

        List<ProductEntity> entities = jpa.findFilteredAll(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                categories,
                unitMeasures,
                valuationMethods,
                request.getManageVariant(),
                request.getStatus(),
                minStock,
                maxStock
        );

        return entities.stream()
                .map(mapper::toDTOFromEntity)
                .toList();
    }


    @Override
    public Page<ProductDTO> findPaginated(ProductViewRequest request) {
        var pageable = PageRequest.of(request.getPage(), request.getSize());

        List<Integer> categories = (request.getCategories() == null || request.getCategories().isEmpty())
                ? null
                : request.getCategories();

        List<Integer> unitMeasures = (request.getUnitMeasures() == null || request.getUnitMeasures().isEmpty())
                ? null
                : request.getUnitMeasures();

        List<Integer> valuationMethods = (request.getValuationMethods() == null || request.getValuationMethods().isEmpty())
                ? null
                : request.getValuationMethods();

        Long minStock = request.getMinimumStock() != null ? request.getMinimumStock().longValue() : null;
        Long maxStock = request.getMaximumStock() != null ? request.getMaximumStock().longValue() : null;

        return jpa.findFiltered(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                categories,
                unitMeasures,
                valuationMethods,
                request.getManageVariant(),
                request.getStatus(),
                minStock,
                maxStock,
                pageable
        ).map(mapper::toDTOFromEntity);
    }


    public ProductViewResponse findPaginatedWithStats(ProductViewRequest request) {
        var page = findPaginated(request);

        List<Integer> categories = (request.getCategories() == null || request.getCategories().isEmpty())
                ? null
                : request.getCategories();

        List<Integer> unitMeasures = (request.getUnitMeasures() == null || request.getUnitMeasures().isEmpty())
                ? null
                : request.getUnitMeasures();

        List<Integer> valuationMethods = (request.getValuationMethods() == null || request.getValuationMethods().isEmpty())
                ? null
                : request.getValuationMethods();

        Long activeCount = jpa.countFiltered(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                categories,
                unitMeasures,
                valuationMethods,
                true,
                request.getMinimumStock() != null ? request.getMinimumStock().longValue() : null,
                request.getMaximumStock() != null ? request.getMaximumStock().longValue() : null
        );

        Long inactiveCount = jpa.countFiltered(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                categories,
                unitMeasures,
                valuationMethods,
                false,
                request.getMinimumStock() != null ? request.getMinimumStock().longValue() : null,
                request.getMaximumStock() != null ? request.getMaximumStock().longValue() : null
        );

        var totalStock = jpa.sumFilteredStock(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                categories,
                unitMeasures,
                valuationMethods,
                request.getStatus(),
                request.getMinimumStock() != null ? request.getMinimumStock().longValue() : null,
                request.getMaximumStock() != null ? request.getMaximumStock().longValue() : null
        );

        return ProductViewResponse.builder()
                .products(page.getContent())
                .totalProducts(page.getTotalElements())
                .activeProducts(activeCount)
                .inactiveProducts(inactiveCount)
                .totalStock(totalStock != null ? totalStock.longValue() : 0L)
                .build();
    }

    @Override
    public ProductDTO findById(Long id) {
        ProductEntity product = jpa.findById(id).orElse(null);
        return product == null ? null : mapper.toDTOFromEntity(product);
    }

    @Override
    public ProductDTO create(ProductCreateRequest request) {
        ProductEntity entity = new ProductEntity();

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCategoryId(request.getCategoryId());
        entity.setUnitMeasureId(request.getUnitMeasureId());
        entity.setValuationMethodId(request.getValuationMethodId());
        entity.setBasePrice(request.getBasePrice());
        entity.setPromoPrice(request.getPromoPrice());
        entity.setBaseCost(request.getBaseCost());

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setCreatedBy(currentUserId);

        ProductEntity saved =jpa.save(entity);
        return mapper.toDTOFromEntity(saved);
    }

    @Override
    public ProductDTO update(ProductUpdateRequest request) {
        ProductEntity entity = new ProductEntity();

        entity.setId(request.getId());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCategoryId(request.getCategoryId());
        entity.setUnitMeasureId(request.getUnitMeasureId());
        entity.setValuationMethodId(request.getValuationMethodId());
        entity.setBasePrice(request.getBasePrice());
        entity.setPromoPrice(request.getPromoPrice());
        entity.setBaseCost(request.getBaseCost());

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setModifiedBy(currentUserId);

        ProductEntity saved =jpa.save(entity);
        return mapper.toDTOFromEntity(saved);
    }

    @Override
    public boolean delete(Long id) {
        ProductEntity entity = jpa.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(currentUserId);
        jpa.save(entity);
        return true;
    }

    @Override
    public boolean updateStatus(Long id) {
        ProductEntity entity = jpa.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(LocalDateTime.now());

        jpa.save(entity);
        return true;
    }

    @Override
    public Product findByCode(String code, long id) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede ser nulo o vacío");
        }

        ProductEntity entity;

        if (id == 0) {
            entity = jpa.findByCode(code).orElse(null);
        } else {
            entity = jpa.findByCodeAndIdNot(code, id).orElse(null);
        }

        return mapper.toDomain(entity);
    }



}
