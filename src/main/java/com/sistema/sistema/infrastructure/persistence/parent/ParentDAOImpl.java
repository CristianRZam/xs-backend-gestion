package com.sistema.sistema.infrastructure.persistence.parent;

import com.sistema.sistema.domain.repository.ParentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ParentDAOImpl implements ParentRepository {

    private final JpaParentRepository jpa;

    public ParentDAOImpl(JpaParentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<ParentEntity> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public ParentEntity save(ParentEntity parent) {
        return jpa.save(parent);
    }
}
