package com.sistema.sistema.infrastructure.persistence.role;

import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.repository.RoleRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RoleDAOImpl implements RoleRepository {
    private final JpaRoleRepository jpa;
    private final RoleMapper mapper;

    public RoleDAOImpl(RoleMapper mapper, JpaRoleRepository jpaRoleRepository) {
        this.mapper = mapper;
        this.jpa = jpaRoleRepository;
    }

    @Override
    public List<Role> findAll() {
        return jpa.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Role> findByDeletedAtIsNull(RoleViewRequest request) {
        List<RoleEntity> entities;

        if (request.getName() == null || request.getName().isEmpty()) {
            entities = jpa.findByDeletedAtIsNullOrderByIdAsc();
        } else {
            entities = jpa.findByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdAsc(request.getName());
        }

        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Role getRoleById(Long id) {
        RoleEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));
        return mapper.toDomain(entity);
    }


    @Override
    public Role save(Role request) {
        RoleEntity entity = mapper.toEntity(request);
        RoleEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Boolean delete(Long id) {
        RoleEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(1L);

        jpa.save(entity);
        return true;
    }

    @Override
    public Role update(Role request) {
        RoleEntity existing = jpa.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Role no encontrado"));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());

        RoleEntity saved = jpa.save(existing);
        return mapper.toDomain(saved);
    }

    @Override
    public Boolean updateStatus(Long id) {
        RoleEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));
        entity.setModifiedBy(currentUserId);

        jpa.save(entity);
        return true;
    }



}
