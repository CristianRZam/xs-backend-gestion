package com.sistema.sistema.infrastructure.persistence.user;

import com.sistema.sistema.domain.model.Person;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.infrastructure.persistence.parameter.JpaParameterRepository;
import com.sistema.sistema.infrastructure.persistence.parameter.ParameterEntity;
import com.sistema.sistema.infrastructure.persistence.permission.PermissionEntity;
import com.sistema.sistema.infrastructure.persistence.person.PersonMapper;
import com.sistema.sistema.infrastructure.persistence.role.RoleEntity;
import com.sistema.sistema.infrastructure.persistence.userrole.UserRoleEntity;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final JpaParameterRepository jpaParameterRepository;
    private final PersonMapper personMapper;

    public UserMapper(JpaParameterRepository jpaParameterRepository, PersonMapper personMapper) {
        this.jpaParameterRepository = jpaParameterRepository;
        this.personMapper = personMapper;
    }

    public User toDomain(UserEntity e) {
        if (e == null) return null;

        // Roles (desde la entidad intermedia UserRoleEntity)
        Set<Role> roles = e.getUserRoles() != null
                ? e.getUserRoles().stream()
                .filter(ur -> ur.getDeletedAt() == null)
                .map(ur -> Role.builder()
                        .id(ur.getRole().getId())
                        .name(ur.getRole().getName())
                        .build())
                .collect(Collectors.toSet())
                : Set.of();

        // Permisos
        Set<String> permissions = e.getUserRoles() != null
                ? e.getUserRoles().stream()
                .filter(ur -> ur.getDeletedAt() == null)
                .flatMap(ur -> ur.getRole().getPermissions().stream())
                .map(PermissionEntity::getName)
                .collect(Collectors.toSet())
                : Set.of();

        // Persona
        Person person = null;
        if (e.getPerson() != null) {
            ParameterEntity typeDoc = jpaParameterRepository
                    .findByParameterIdAndCode(e.getPerson().getTypeDocument(), "TIPO_DOCUMENTO")
                    .orElse(null);

            person = Person.builder()
                    .id(e.getPerson().getId())
                    .document(e.getPerson().getDocument())
                    .fullName(e.getPerson().getFullName())
                    .phone(e.getPerson().getPhone())
                    .address(e.getPerson().getAddress())
                    .typeDocument(e.getPerson().getTypeDocument())
                    .typeDocumentName(typeDoc != null ? typeDoc.getName() : null)
                    .build();
        }

        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .email(e.getEmail())
                .password(e.getPassword())
                .active(e.getActive())
                .deleted(e.getDeletedAt() != null)
                .roles(roles)
                .permissions(permissions)
                .person(person)
                .build();
    }

    public UserEntity toEntity(User d) {
        if (d == null) return null;

        UserEntity entity = UserEntity.builder()
                .id(d.getId())
                .username(d.getUsername())
                .email(d.getEmail())
                .password(d.getPassword())
                .active(d.getActive())
                .person(d.getPerson() != null ? personMapper.toEntity(d.getPerson()) : null)
                .build();

        // Mapear roles a UserRoleEntity
        if (d.getRoles() != null && !d.getRoles().isEmpty()) {
            entity.setUserRoles(d.getRoles().stream()
                    .map(r -> UserRoleEntity.builder()
                            .user(entity)
                            .role(RoleEntity.builder()
                                    .id(r.getId())
                                    .name(r.getName())
                                    .build())
                            .assignedBy(1L) // por defecto, o se puede pasar dinámicamente
                            .build())
                    .collect(Collectors.toSet()));
        }

        return entity;
    }
}
