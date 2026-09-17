package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigCreateRequest;
import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigUpdateRequest;
import com.sistema.sistema.application.dto.response.catalogconfig.CatalogConfigDTO;
import com.sistema.sistema.domain.repository.CatalogConfigRepository;
import com.sistema.sistema.domain.usecase.CatalogConfigUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogConfigService implements CatalogConfigUseCase {

    private final CatalogConfigRepository repository;

    public CatalogConfigService(CatalogConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CatalogConfigDTO> findAll() {
        return repository.findAll();
    }

    @Override
    public CatalogConfigDTO findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public CatalogConfigDTO create(CatalogConfigCreateRequest request) {
        return repository.create(request);
    }

    @Override
    public CatalogConfigDTO update(CatalogConfigUpdateRequest request) {
        return repository.update(request);
    }

    @Override
    public boolean delete(Long id) {
        return repository.delete(id);
    }

}