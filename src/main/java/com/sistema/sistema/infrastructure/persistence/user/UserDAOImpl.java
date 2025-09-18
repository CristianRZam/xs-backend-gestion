package com.sistema.sistema.infrastructure.persistence.user;


import com.sistema.sistema.application.dto.request.user.UserViewRequest;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import com.sistema.sistema.infrastructure.persistence.parameter.ParameterEntity;
import com.sistema.sistema.infrastructure.persistence.role.RoleEntity;
import com.sistema.sistema.infrastructure.persistence.userrole.UserRoleEntity;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserDAOImpl implements UserRepository{

    private final JpaUserRepository jpa;
    private final UserMapper mapper;

    public UserDAOImpl(JpaUserRepository jpa, UserMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmailWithRolesAndPerson(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsernameWithRolesAndPerson(username).map(mapper::toDomain);
    }


    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setCreatedBy(currentUserId);

        UserEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public User update(User existingUser) {
        UserEntity entity = jpa.findById(existingUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + existingUser.getId()));

        Long currentUserId = SecurityUtil.getCurrentUserId();

        // --- Actualizar usuario ---
        entity.setUsername(existingUser.getUsername());
        entity.setEmail(existingUser.getEmail());
        entity.setModifiedBy(currentUserId);

        // --- Actualizar persona ---
        if (entity.getPerson() != null && existingUser.getPerson() != null) {
            entity.getPerson().setTypeDocument(existingUser.getPerson().getTypeDocument());
            entity.getPerson().setDocument(existingUser.getPerson().getDocument());
            entity.getPerson().setFullName(existingUser.getPerson().getFullName());
            entity.getPerson().setPhone(existingUser.getPerson().getPhone());
            entity.getPerson().setAddress(existingUser.getPerson().getAddress());
        }

        // --- Actualizar roles ---
        Set<Long> rolesEnviados = existingUser.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        // 1. Marcar eliminados los que no vienen
        for (UserRoleEntity ur : entity.getUserRoles()) {
            if (!rolesEnviados.contains(ur.getRole().getId()) && ur.getDeletedAt() == null) {
                ur.setModifiedBy(currentUserId);
                ur.setDeletedAt(LocalDateTime.now());
                ur.setDeletedBy(currentUserId);
            }
        }

        // 2. Agregar o reactivar roles
        for (Role role : existingUser.getRoles()) {
            Optional<UserRoleEntity> existente = entity.getUserRoles()
                    .stream()
                    .filter(ur -> ur.getRole().getId().equals(role.getId()))
                    .findFirst();

            if (existente.isPresent()) {
                if (existente.get().getDeletedAt() != null) {
                    existente.get().setModifiedBy(currentUserId);
                    existente.get().setDeletedAt(null); // reactivar
                    existente.get().setDeletedBy(null);
                }
            } else {
                RoleEntity roleRef = new RoleEntity();
                roleRef.setId(role.getId());

                UserRoleEntity nuevo = UserRoleEntity.builder()
                        .user(entity)
                        .role(roleRef)
                        .assignedBy(currentUserId)
                        .createdBy(currentUserId)
                        .assignedAt(LocalDateTime.now())
                        .build();

                entity.getUserRoles().add(nuevo);
            }
        }

        UserEntity updated = jpa.save(entity);
        return mapper.toDomain(updated);
    }


    @Override
    public List<User> ByDeletedAtIsNull(UserViewRequest request) {
        List<UserEntity> entities;

        if (request.getName() == null || request.getName().isEmpty()) {
            entities = jpa.findByDeletedAtIsNullOrderByIdAsc();
        } else {
            entities = jpa.findByDeletedAtIsNullAndUsernameContainingIgnoreCaseOrderByIdAsc(request.getName());
        }

        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public User getUserById(Long id) {
        UserEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        return mapper.toDomain(entity);
    }

    @Override
    public Boolean delete(Long id) {
        // 1. Buscar usuario
        UserEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        // 2. Recuperar el ID del usuario logueado desde el SecurityContext
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 3. Marcar como eliminado
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(currentUserId);
        entity.setActive(false);

        // 4. Guardar cambios
        jpa.save(entity);

        return true;
    }

    @Override
    public Boolean updateStatus(Long id) {
        UserEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        Long currentUserId = SecurityUtil.getCurrentUserId();

        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));
        entity.setModifiedBy(currentUserId);
        jpa.save(entity);
        return true;
    }

}
