package com.sistema.sistema.infrastructure.persistence.catalogconfig;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCatalogConfigRepository extends JpaRepository<CatalogConfigEntity, Long> {

    List<CatalogConfigEntity> findByDeletedAtIsNull();

}
