package com.sistema.sistema.infrastructure.persistence.user;

import com.sistema.sistema.application.dto.response.role.RoleDto;
import com.sistema.sistema.application.dto.response.user.UserDto;
import com.sistema.sistema.application.dto.response.user.UserRoleDTO;
import com.sistema.sistema.domain.model.*;
import com.sistema.sistema.infrastructure.persistence.parameter.JpaParameterRepository;
import com.sistema.sistema.infrastructure.persistence.parameter.ParameterEntity;
import com.sistema.sistema.infrastructure.persistence.permission.PermissionMapper;
import com.sistema.sistema.infrastructure.persistence.person.PersonMapper;
import com.sistema.sistema.infrastructure.persistence.role.RoleEntity;
import com.sistema.sistema.infrastructure.persistence.rolepermission.RolePermissionEntity;
import com.sistema.sistema.infrastructure.persistence.userrole.UserRoleEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final JpaParameterRepository jpaParameterRepository;
    private final PersonMapper personMapper;
    private final PermissionMapper permissionMapper;

    public UserMapper(JpaParameterRepository jpaParameterRepository, PersonMapper personMapper, PermissionMapper permissionMapper) {
        this.jpaParameterRepository = jpaParameterRepository;
        this.personMapper = personMapper;
        this.permissionMapper = permissionMapper;
    }

    public User toDomain(UserEntity e) {
        if (e == null) return null;

        // Mapear roles
        Set<UserRole> userRoles = e.getUserRoles() != null
                ? e.getUserRoles().stream()
                .filter(ur -> ur.getDeletedAt() == null) // solo user_roles activos
                .map(ur -> {
                    RoleEntity roleEntity = ur.getRole();
                    if (roleEntity == null) return null;

                    // Permisos del rol
                    List<Permission> permissions = roleEntity.getRolePermissions() != null
                            ? roleEntity.getRolePermissions().stream()
                            .filter(rp -> rp.getDeletedAt() == null) // solo permisos activos
                            .map(RolePermissionEntity::getPermission)
                            .map(permissionMapper::toDomain)
                            .collect(Collectors.toList())
                            : List.of();

                    Role role = Role.builder()
                            .id(roleEntity.getId())
                            .name(roleEntity.getName())
                            .description(roleEntity.getDescription())
                            .active(roleEntity.getActive())
                            .permissions(permissions)
                            .createdBy(roleEntity.getCreatedBy())
                            .createdAt(roleEntity.getCreatedAt())
                            .modifiedBy(roleEntity.getModifiedBy())
                            .modifiedAt(roleEntity.getModifiedAt())
                            .deletedBy(roleEntity.getDeletedBy())
                            .deletedAt(roleEntity.getDeletedAt())
                            .build();

                    return UserRole.builder()
                            .id(ur.getId())
                            .deleted(ur.getDeletedAt() != null)
                            .role(role)
                            .build();
                })
                .filter(Objects::nonNull)
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
                .roles(userRoles)
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

        if (d.getRoles() != null && !d.getRoles().isEmpty()) {
            entity.setUserRoles(d.getRoles().stream()
                    .map(ur -> UserRoleEntity.builder()
                            .id(ur.getId())
                            .user(entity)
                            .role(RoleEntity.builder()
                                    .id(ur.getRole().getId())
                                    .name(ur.getRole().getName())
                                    .build())
                            .assignedBy(1L) // se puede parametrizar
                            .deletedAt(ur.getDeleted() != null && ur.getDeleted() ? LocalDateTime.now() : null)
                            .build())
                    .collect(Collectors.toSet()));
        }

        return entity;
    }

    public UserDto toDto(User user) {
        if (user == null) return null;

        List<UserRoleDTO> userRoleDtos = null;
        Set<UserRole> userRoles = user.getRoles();

        if (userRoles != null) {
            userRoleDtos = userRoles.stream()
                    .map(ur -> {
                        Role role = ur.getRole();
                        RoleDto roleDto = null;

                        if (role != null) {
                            roleDto = RoleDto.builder()
                                    .id(role.getId())
                                    .name(role.getName())
                                    .description(role.getDescription())
                                    .active(role.getActive())
                                    .deleted(role.getDeletedAt() != null || role.getDeletedBy() != null)
                                    .build();
                        }

                        return UserRoleDTO.builder()
                                .id(ur.getId())
                                .role(roleDto)
                                .deleted(Boolean.TRUE.equals(ur.getDeleted()))
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(Boolean.TRUE.equals(user.getActive()))
                .deleted(user.getDeleted())
                .userRoles(userRoleDtos)
                .person(user.getPerson())
                .build();
    }

}
