package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.product.ProductCreateRequest;
import com.sistema.sistema.application.dto.request.product.ProductFormRequest;
import com.sistema.sistema.application.dto.request.product.ProductUpdateRequest;
import com.sistema.sistema.application.dto.request.product.ProductViewRequest;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.product.ProductFormResponse;
import com.sistema.sistema.application.dto.response.product.ProductViewResponse;
import com.sistema.sistema.application.dto.response.productimage.ProductImageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ProductUseCase {

    List<ProductDTO> findAllFiltered(ProductViewRequest request);

    ProductViewResponse init(ProductViewRequest request);

    ProductFormResponse initFormData(ProductFormRequest request);

    ProductDTO create(ProductCreateRequest request, MultipartFile[] images, String mainImageKey);

    ProductDTO update(ProductUpdateRequest request, MultipartFile[] images, List<ProductImageDTO> imagesToKeep, String mainImageKey);

    boolean delete(Long id);

    boolean updateStatus(Long id);

}
