package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.profile.PasswordRequest;
import com.sistema.sistema.application.dto.request.profile.ProfileUpdateRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.profile.ProfileDTO;
import com.sistema.sistema.application.dto.response.profile.ProfileFormresponse;
import com.sistema.sistema.application.dto.response.profile.ProfileViewResponse;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.model.Permission;
import com.sistema.sistema.domain.model.Person;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.ParameterRepository;
import com.sistema.sistema.domain.repository.PersonRepository;
import com.sistema.sistema.domain.repository.UserRepository;
import com.sistema.sistema.domain.usecase.ProfileUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.persistence.parameter.ParameterMapper;
import com.sistema.sistema.infrastructure.persistence.profile.ProfileMapper;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProfileService implements ProfileUseCase {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.backend-url}")
    private String backendUrl;

    private final UserRepository repository;
    private final ParameterRepository parameterRepository;
    private final PersonRepository personRepository;
    private final ProfileMapper mapper;
    private final ParameterMapper parameterMapper;
    private final PasswordEncoder passwordEncoder;
    public ProfileService(
            UserRepository repository,
            ProfileMapper mapper,
            ParameterRepository parameterRepository,
            ParameterMapper parameterMapper,
            PersonRepository personRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.parameterRepository = parameterRepository;
        this.parameterMapper = parameterMapper;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ProfileViewResponse init() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = repository.getUserById(currentUserId);
        ProfileDTO dto = mapper.toProfileDTO(user);
        return ProfileViewResponse.builder()
                .user(dto)
                .build();
    }

    @Override
    public ProfileFormresponse initFormData() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = repository.getUserById(currentUserId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    "Usuario no encontrado.");
        }

        // Traer tipos de documento
        List<Parameter> types = parameterRepository.getListParameterByCode("TIPO_DOCUMENTO");

        // Mapear a DTO
        List<ParameterDto> typeDtos = parameterMapper.toDtoList(types);

        // Mapear usuario
        ProfileDTO dto = mapper.toProfileDTO(user);

        // Retornar con ambos datos
        return ProfileFormresponse.builder()
                .user(dto)
                .documentTypes(typeDtos)
                .build();
    }


    @Override
    public ProfileDTO update(ProfileUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 1. Validar que solo edita su propio perfil
        if (!Objects.equals(currentUserId, request.getIdUser())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "No tiene permisos para modificar este usuario."
            );
        }

        // 2. Validar que exista el usuario con ese id
        User existingUser = repository.getUserById(request.getIdUser());
        if (existingUser == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    "Usuario no encontrado con id: " + request.getIdUser());
        }

        // 3. Validar unicidad de documento (excluyendo su propio documento)
        Long currentPersonId = existingUser.getPerson() != null ? existingUser.getPerson().getId() : null;
        personRepository.findByDocument(request.getDocument()).ifPresent(existingPerson -> {
            if (!Objects.equals(existingPerson.getId(), currentPersonId)) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el nº de documento: " + request.getDocument());
            }
        });

        // 4. Validar unicidad de email (excluyendo su propio email)
        repository.findByEmail(request.getEmail()).ifPresent(u -> {
            if (!Objects.equals(u.getId(), request.getIdUser())) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el correo: " + request.getEmail());
            }
        });

        // 5. Actualizar datos de la persona
        Person person = existingUser.getPerson();
        if (person != null) {
            person.setTypeDocument(request.getTypeDocument());
            person.setDocument(request.getDocument());
            person.setFullName(request.getFullName());
            person.setPhone(request.getPhone());
            person.setAddress(request.getAddress());
        }

        // 6. Actualizar datos del usuario
        existingUser.setEmail(request.getEmail());

        // 7. Guardamos los datos modificados
        User userSaved = repository.update(existingUser);

        // 8. Retornamos el profile mapeado
        return mapper.toProfileDTO(userSaved);
    }

    @Override
    public Boolean updatePassword(PasswordRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = repository.getUserById(currentUserId);

        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }

        // 1. Validar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta.");
        }

        // 2. Validar que la nueva no sea igual a la actual
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La nueva contraseña no puede ser igual a la actual.");
        }

        // 3. Validar que la confirmación coincida
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La confirmación no coincide con la nueva contraseña.");
        }

        // 4. Encriptar y actualizar
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);


        return repository.updatePassword(user);
    }


    @Override
    public Boolean disableAccount() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = repository.getUserById(currentUserId);

        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }

        if (!user.getActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La cuenta ya está deshabilitada.");
        }


        return repository.disableAccount(user);
    }


    @Override
    public ProfileDTO uploadAvatar(MultipartFile file) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = repository.getUserById(currentUserId);

        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }

        if (file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No se ha seleccionado un archivo.");
        }

        try {
            // Crear carpeta de avatares si no existe
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir, "uploads", "avatars");
            java.nio.file.Files.createDirectories(uploadPath);

            // Eliminar avatar anterior si existe
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String oldFileName = user.getAvatarUrl().substring(user.getAvatarUrl().lastIndexOf("/") + 1);
                java.nio.file.Path oldFilePath = uploadPath.resolve(oldFileName);
                if (java.nio.file.Files.exists(oldFilePath)) {
                    java.nio.file.Files.delete(oldFilePath);
                }
            }

            // Generar nuevo nombre de archivo
            String fileName = "avatar_" + currentUserId + "_" + System.currentTimeMillis() +
                    file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            java.nio.file.Path newPath = uploadPath.resolve(fileName);

            // Guardar archivo
            file.transferTo(newPath.toFile());

            // Guardar URL completa en BD
            String fullUrl = backendUrl + "/uploads/avatars/" + fileName;
            user.setAvatarUrl(fullUrl);
            user = repository.updateAvatar(user);

            return mapper.toProfileDTO(user);

        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir el avatar: " + e.getMessage());
        }
    }

    @Override
    public ProfileDTO deleteAvatar() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = repository.getUserById(currentUserId);

        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }

        try {
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String fileName = user.getAvatarUrl().substring(user.getAvatarUrl().lastIndexOf("/") + 1);
                java.nio.file.Path path = java.nio.file.Paths.get(uploadDir, "uploads", "avatars", fileName);

                // Eliminar archivo si existe
                java.nio.file.Files.deleteIfExists(path);

                // Limpiar URL en BD
                user.setAvatarUrl(null);
                repository.updateAvatar(user);
            }

            return mapper.toProfileDTO(user);

        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar el avatar: " + e.getMessage());
        }
    }

    @Override
    public List<Permission> loadMyPermissions() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = repository.getUserById(currentUserId);

        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }

        return user.getRoles().stream()
                .filter(ur -> !ur.getDeleted())
                .flatMap(ur -> ur.getRole().getPermissions().stream())
                .distinct()
                .collect(Collectors.toList());
    }



}
