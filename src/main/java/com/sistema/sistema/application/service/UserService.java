package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.user.UserCreateRequest;
import com.sistema.sistema.application.dto.request.user.UserFormRequest;
import com.sistema.sistema.application.dto.request.user.UserUpdateRequest;
import com.sistema.sistema.application.dto.request.user.UserViewRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.role.RoleDto;
import com.sistema.sistema.application.dto.response.user.UserDto;
import com.sistema.sistema.application.dto.response.user.UserFormResponse;
import com.sistema.sistema.application.dto.response.user.UserViewResponse;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.model.Person;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.*;
import com.sistema.sistema.domain.usecase.UserUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.persistence.user.UserMapper;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserUseCase {

    private final UserRepository repository;
    private final ParameterRepository parameterRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper mapper;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public UserService(
            UserRepository userRepository,
            ParameterRepository parameterRepository,
            PersonRepository personRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            UserMapper mapper
    ) {
        this.repository = userRepository;
        this.parameterRepository = parameterRepository;
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.mapper = mapper;
    }

    @Override
    public UserViewResponse init(UserViewRequest request) {
        List<User> users = repository.ByDeletedAtIsNull(request);

        // Total de usuarios
        Long total = (long) users.size();
        // Contamos usuarios activos
        Long activeUsers = users.stream() .filter(User::getActive) .count();
        // Usuarios inactivos
        Long inactiveUsers = total - activeUsers;
        // Usuarios con rol ADMIN (id = 1)
        Long totalAdmins = users.stream() .filter(u -> u.getRoles() != null)
            .filter(u -> u.getRoles().stream().anyMatch(r -> r.getId() == 1L)) .count();
        // 🔹 Convertimos la lista de User -> UserDto con el mapper
        List<UserDto> userDtos = users.stream() .map(mapper::toDto) .collect(Collectors.toList());

        // Tipos de documento
        List<Parameter> types = parameterRepository.getListParameterByCode("TIPO_DOCUMENTO");
        List<ParameterDto> typeDtos = types != null
                ? types.stream()
                .map(param -> ParameterDto.builder()
                        .id(param.getId())
                        .parentParameterId(param.getParentParameterId())
                        .parameterId(param.getParameterId())
                        .code(param.getCode())
                        .type(param.getType())
                        .name(param.getName())
                        .shortName(param.getShortName())
                        .orderNumber(param.getOrderNumber())
                        .active(Boolean.TRUE.equals(param.getActive()))
                        .deleted(param.getDeletedAt() != null)
                        .build())
                .toList()
                : List.of();

        return UserViewResponse.builder()
                .users(userDtos)
                .typeDocuments(typeDtos)
                .totalUsers(total)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .totalAdmins(totalAdmins)
                .build();
    }



    @Override
    public UserFormResponse initFormData(UserFormRequest request) {
        User user = null;
        if (request.getId() != null) {
            user = repository.getUserById(request.getId());
        }

        List<Parameter> types = parameterRepository.getListParameterByCode("TIPO_DOCUMENTO");
        List<Role> roles = roleRepository.findAll();

        UserDto userDto = null;
        if (user != null) {
            userDto = UserDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .active(Boolean.TRUE.equals(user.getActive()))
                    .deleted(Boolean.TRUE.equals(user.getDeleted()))
                    .roles(user.getRoles() != null
                            ? user.getRoles().stream()
                            .map(role -> RoleDto.builder()
                                    .id(role.getId())
                                    .name(role.getName())
                                    .description(role.getDescription())
                                    .active(Boolean.TRUE.equals(role.getActive()))
                                    .deleted(role.getDeletedAt() != null)
                                    .build()
                            ).toList()
                            : List.of())
                    .person(user.getPerson())
                    .build();
        }

        List<ParameterDto> typeDtos = types != null
                ? types.stream().map(param -> ParameterDto.builder()
                .id(param.getId())
                .parentParameterId(param.getParentParameterId())
                .parameterId(param.getParameterId())
                .code(param.getCode())
                .type(param.getType())
                .name(param.getName())
                .shortName(param.getShortName())
                .orderNumber(param.getOrderNumber())
                .active(Boolean.TRUE.equals(param.getActive()))
                .deleted(param.getDeletedAt() != null)
                .build()
        ).toList()
                : List.of();

        List<RoleDto> roleDtos = roles != null
                ? roles.stream().map(role -> RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(Boolean.TRUE.equals(role.getActive()))
                .deleted(role.getDeletedAt() != null)
                .build()
        ).toList()
                : List.of();

        return UserFormResponse.builder()
                .user(userDto)
                .documentTypes(typeDtos)
                .roles(roleDtos)
                .build();
    }




    @Override
    public User getUserById(Long id) {
        return repository.getUserById(id);
    }

    @Override
    @Transactional
    public User create(UserCreateRequest request) {
        // 0. Validaciones de unicidad antes de crear
        repository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con el correo: " + request.getEmail());
        });

        repository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con el nombre de usuario: " + request.getUsername());
        });

        personRepository.findByDocument(request.getDocument()).ifPresent(p -> {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con el nº de documento: " + request.getDocument());
        });

        // 4. Validar existencia de roles antes de crear
        Set<Role> roles = new HashSet<>();
        Set<Long> faltantes = new HashSet<>();

        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.getRoleById(roleId);
            if (role != null) {
                roles.add(role);
            } else {
                faltantes.add(roleId);
            }
        }

        if (!faltantes.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    "Los roles con id " + faltantes + " no existen");
        }

        // 2. Guardar persona
        Person person = Person.builder()
                .typeDocument(request.getTypeDocument())
                .document(request.getDocument())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        Person savedPerson = personRepository.save(person);

        // 3. Crear usuario asociado a esa persona
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getDocument()))
                .active(true)
                .person(savedPerson)
                .build();

        // 4. Guardar usuario usando el repositorio de dominio
        User savedUser = repository.save(user);

        // 5. Asignar roles
        userRoleRepository.saveUserRoles(savedUser, roles, 1L);
        savedUser.setRoles(roles);

        return savedUser;
    }


    @Override
    @Transactional
    public User update(UserUpdateRequest request) {
        // 1. Obtener usuario existente (domain)
        User existingUser = repository.getUserById(request.getId());
        if (existingUser == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    "Usuario no encontrado con id: " + request.getId());
        }

        Long currentPersonId = existingUser.getPerson() != null
                ? existingUser.getPerson().getId()
                : null;

        // 2. Validar unicidad de email
        repository.findByEmail(request.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(request.getId())) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el correo: " + request.getEmail());
            }
        });

        // 3. Validar unicidad de username
        repository.findByUsername(request.getUsername()).ifPresent(u -> {
            if (!u.getId().equals(request.getId())) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el nombre de usuario: " + request.getUsername());
            }
        });

        // 4. Validar unicidad de documento
        personRepository.findByDocument(request.getDocument()).ifPresent(existingPerson -> {
            if (!existingPerson.getId().equals(currentPersonId)) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el nº de documento: " + request.getDocument());
            }
        });

        // 5. Validar existencia de roles antes de actualizar
        Set<Role> roles = new HashSet<>();
        Set<Long> faltantes = new HashSet<>();

        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.getRoleById(roleId);
            if (role != null) {
                roles.add(role);
            } else {
                faltantes.add(roleId);
            }
        }

        if (!faltantes.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    "Los roles con id " + faltantes + " no existen");
        }

        // 6. Actualizar datos de la persona dentro del dominio
        Person person = existingUser.getPerson();
        person.setTypeDocument(request.getTypeDocument());
        person.setDocument(request.getDocument());
        person.setFullName(request.getFullName());
        person.setPhone(request.getPhone());
        person.setAddress(request.getAddress());

        // 7. Actualizar datos del usuario
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());

        // 8. Actualizar roles
        existingUser.setRoles(roles);

        // 9. Guardar cambios
        return repository.update(existingUser);
    }


    @Override
    public Boolean delete(Long id) {
        User existingUser = repository.getUserById(id);
        if (existingUser == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    "Usuario no encontrado con id: " + id);
        }

        personRepository.delete(existingUser.getPerson().getId());

        return repository.delete(id);
    }

    @Override
    public Boolean updateStatus(Long id) {
        return repository.updateStatus(id);
    }
}
