package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.product.ProductCreateRequest;
import com.sistema.sistema.application.dto.request.product.ProductUpdateRequest;
import com.sistema.sistema.application.dto.request.product.ProductViewRequest;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.product.ProductViewResponse;
import com.sistema.sistema.domain.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductRepository {
    Page<ProductDTO> findPaginated(ProductViewRequest request);

    List<ProductDTO> findAllFiltered(ProductViewRequest request);

    ProductViewResponse findPaginatedWithStats(ProductViewRequest request);

    ProductDTO findById(Long id);

    ProductDTO create(ProductCreateRequest request);

    ProductDTO update(ProductUpdateRequest request);

    boolean delete(Long id);

    boolean updateStatus(Long id);

    Product findByCode(String code, long id);

    boolean updateStock(Long id, long stock);

    boolean reserveStock(Long id, long quantity);

    void releaseReservedStock(Long id, long quantity);

    boolean consumeAvailableStock(Long id, long quantity);

    boolean consumeReservedStock(Long id, long quantity);
}
