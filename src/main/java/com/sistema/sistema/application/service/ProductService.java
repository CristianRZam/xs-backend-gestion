package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.product.ProductCreateRequest;
import com.sistema.sistema.application.dto.request.product.ProductFormRequest;
import com.sistema.sistema.application.dto.request.product.ProductUpdateRequest;
import com.sistema.sistema.application.dto.request.product.ProductViewRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.product.ProductFormResponse;
import com.sistema.sistema.application.dto.response.product.ProductViewResponse;
import com.sistema.sistema.application.dto.response.productimage.ProductImageDTO;
import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import com.sistema.sistema.domain.model.Product;
import com.sistema.sistema.domain.repository.ParameterRepository;
import com.sistema.sistema.domain.repository.ProductImageRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.usecase.FileStorageUseCase;
import com.sistema.sistema.domain.usecase.ProductUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductService implements ProductUseCase {

    private final ProductRepository repository;
    private final ParameterRepository parameterRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageUseCase fileStorageUseCase;

    public ProductService(ProductRepository repository, ParameterRepository parameterRepository,
                          ProductImageRepository productImageRepository,  FileStorageUseCase fileStorageUseCase) {
        this.repository = repository;
        this.parameterRepository = parameterRepository;
        this.productImageRepository = productImageRepository;
        this.fileStorageUseCase = fileStorageUseCase;
    }

    @Override
    public List<ProductDTO> findAllFiltered(ProductViewRequest request) {
        return  repository.findAllFiltered(request);
    }

    @Override
    public ProductViewResponse init(ProductViewRequest request) {
        ProductViewResponse response = repository.findPaginatedWithStats(request);
        List<ParameterDto> categories = parameterRepository.getListParameterByCode("CATEGORIA_PRODUCTO");
        List<ParameterDto> unitMeasures = parameterRepository.getListParameterByCode("UNIDAD_MEDIDA_PRODUCTO");
        List<ParameterDto> valuationMethods = parameterRepository.getListParameterByCode("METODO_VALUACION");
        response.setCategories(categories);
        response.setUnitMeasures(unitMeasures);
        response.setValuationMethods(valuationMethods);
        return response;
    }

    @Override
    public ProductFormResponse initFormData(ProductFormRequest request) {
        ProductFormResponse response = new ProductFormResponse();
       if(request.getId() != null){
           ProductDTO product = repository.findById(request.getId());
           response.setProduct(product);

           List<ProductImageDTO> images =
                   productImageRepository.findByProductIdAndActiveTrue(request.getId());
           response.setImages(images);
       }
        List<ParameterDto> categories = parameterRepository.getListParameterByCode("CATEGORIA_PRODUCTO");
        List<ParameterDto> unitMeasures = parameterRepository.getListParameterByCode("UNIDAD_MEDIDA_PRODUCTO");
        List<ParameterDto> valuationMethods = parameterRepository.getListParameterByCode("METODO_VALUACION");
        response.setCategories(categories);
        response.setUnitMeasures(unitMeasures);
        response.setValuationMethods(valuationMethods);
        return response;
    }

    @Override
    public ProductDTO create(
            ProductCreateRequest request,
            MultipartFile[] images,
            String mainImageKey
    ) {

        Product codigoProducto = repository.findByCode(request.getCode(), 0);
        if (codigoProducto != null) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "El código '" + request.getCode() + "' ya pertenece a otro producto."
            );
        }

        // Crear producto
        ProductDTO product = repository.create(request);

        if (images != null && images.length > 0) {
            try {
                List<StoredFileDTO> storedImages = fileStorageUseCase.upload(
                        "products/" + product.getId(),
                        images
                );

                // Guardar imágenes
                List<ProductImageDTO> savedImages =
                        productImageRepository.saveImages(product.getId(), storedImages);

                // Marcar principal
                resolveMainImage(
                        product.getId(),
                        savedImages,
                        List.of(),
                        mainImageKey
                );

            } catch (Exception e) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al subir imágenes del producto"
                );
            }
        }

        return product;
    }



    @Override
    public ProductDTO update(
            ProductUpdateRequest request,
            MultipartFile[] images,
            List<ProductImageDTO> imagesToKeep,
            String mainImageKey
    ) {

        // Validar código
        Product codigoProducto = repository.findByCode(request.getCode(), request.getId());
        if (codigoProducto != null) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "El código '" + request.getCode() + "' ya pertenece a otro producto."
            );
        }

        // Actualizar producto
        ProductDTO product = repository.update(request);

        // Obtener imágenes actuales
        List<ProductImageDTO> currentImages =
                productImageRepository.findByProductId(product.getId());

        List<Long> keepIds = imagesToKeep == null
                ? List.of()
                : imagesToKeep.stream().map(ProductImageDTO::getId).toList();

        List<ProductImageDTO> imagesToDelete = currentImages.stream()
                .filter(img -> !keepIds.contains(img.getId()))
                .toList();

        // Eliminar imágenes
        imagesToDelete.forEach(img ->
                productImageRepository.deleteById(img.getId())
        );

        // Subir nuevas imágenes
        List<ProductImageDTO> newImages = List.of();
        if (images != null && images.length > 0) {
            try {
                List<StoredFileDTO> storedImages = fileStorageUseCase.upload(
                        "products/" + product.getId(),
                        images
                );
                newImages = productImageRepository.saveImages(
                        product.getId(),
                        storedImages
                );
            } catch (Exception e) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al subir nuevas imágenes del producto"
                );
            }
        }

        // Resolver principal
        resolveMainImage(
                product.getId(),
                newImages,
                imagesToKeep != null ? imagesToKeep : List.of(),
                mainImageKey
        );

        return product;
    }


    @Override
    public boolean delete(Long id) {
        return repository.delete(id);
    }

    @Override
    public boolean updateStatus(Long id) {
        return repository.updateStatus(id);
    }


    private void resolveMainImage(
            Long productId,
            List<ProductImageDTO> newImages,
            List<ProductImageDTO> existingImages,
            String mainImageKey
    ) {

        // Resetear todas
        productImageRepository.clearMainByProduct(productId);

        if (mainImageKey == null || mainImageKey.isBlank()) {
            return;
        }

        // Normalizar key (quita espacios y comillas)
        String key = mainImageKey.trim();

        if (key.isBlank()) {
            return;
        }

        if (key.startsWith("\"") && key.endsWith("\"")) {
            key = key.substring(1, key.length() - 1);
        }

        // Imagen existente
        if (key.startsWith("existing:")) {
            Long id = Long.parseLong(key.replace("existing:", ""));
            productImageRepository.markAsMain(id);
            return;
        }

        // Imagen nueva
        if (key.startsWith("temp:")) {
            int index = Integer.parseInt(key.replace("temp:", ""));
            if (index >= 0 && index < newImages.size()) {
                productImageRepository.markAsMain(
                        newImages.get(index).getId()
                );
            }
        }

    }

}
