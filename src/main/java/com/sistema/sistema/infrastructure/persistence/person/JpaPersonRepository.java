package com.sistema.sistema.infrastructure.persistence.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPersonRepository extends JpaRepository<PersonEntity, Long> {
    Optional<PersonEntity> findByDocument(String document);
}
