package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.product.ProductCreateRequest;
import com.sistema.sistema.application.dto.request.product.ProductFormRequest;
import com.sistema.sistema.application.dto.request.product.ProductUpdateRequest;
import com.sistema.sistema.application.dto.request.product.ProductViewRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.product.ProductFormResponse;
import com.sistema.sistema.application.dto.response.product.ProductViewResponse;
import com.sistema.sistema.domain.model.Product;
import com.sistema.sistema.domain.repository.ParameterRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.usecase.ProductUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService implements ProductUseCase {

    private final ProductRepository repository;
    private final ParameterRepository parameterRepository;

    public ProductService(ProductRepository repository, ParameterRepository parameterRepository) {
        this.repository = repository;
        this.parameterRepository = parameterRepository;
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
    public ProductDTO create(ProductCreateRequest request) {
        Product codigoProducto = repository.findByCode(request.getCode(), 0);
        if (codigoProducto != null) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "El código '" + request.getCode() + "' ya pertenece a otro producto."
            );
        }
        return repository.create(request);
    }

    @Override
    public ProductDTO update(ProductUpdateRequest request) {
        Product codigoProducto = repository.findByCode(request.getCode(), request.getId());
        if (codigoProducto != null) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "El código '" + request.getCode() + "' ya pertenece a otro producto."
            );
        }
        return repository.update(request);
    }

    @Override
    public boolean delete(Long id) {
        return repository.delete(id);
    }

    @Override
    public boolean updateStatus(Long id) {
        return repository.updateStatus(id);
    }

}
