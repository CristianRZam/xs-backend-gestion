package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigCreateRequest;
import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigUpdateRequest;
import com.sistema.sistema.application.dto.response.catalogconfig.CatalogConfigDTO;

import java.util.List;

public interface CatalogConfigRepository {

    List<CatalogConfigDTO> findAll();

    CatalogConfigDTO findById(Long id);

    CatalogConfigDTO create(CatalogConfigCreateRequest request);

    CatalogConfigDTO update(CatalogConfigUpdateRequest request);

    boolean delete(Long id);

}