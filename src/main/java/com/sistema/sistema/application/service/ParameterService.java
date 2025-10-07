package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.parameter.ParameterCreateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterFormRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterUpdateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterViewRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.parameter.ParameterFormResponse;
import com.sistema.sistema.application.dto.response.parameter.ParameterViewResponse;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.repository.ParameterRepository;
import com.sistema.sistema.domain.usecase.ParameterUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.persistence.parameter.ParameterMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;


@Service
public class ParameterService implements ParameterUseCase {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.backend-url}")
    private String backendUrl;

    private final ParameterRepository repository;
    private final ParameterMapper mapper;

    public ParameterService(ParameterRepository repository, ParameterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ParameterViewResponse init(ParameterViewRequest request) {
        List<Parameter> parameters = repository.findByDeletedAtIsNull(request);

        // Total de parámetros
        Long total = (long) parameters.size();

        // Contamos parámetros activos
        Long activeParameters = parameters.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .count();

        // Parámetros inactivos
        Long inactiveParameters = total - activeParameters;

        // Parámetros que tienen parentParameterId
        Long parametersWithParent = parameters.stream()
                .filter(p -> p.getParentParameterId() != null)
                .count();

        // Lista completa a DTO
        List<ParameterDto> parameterDtos = mapper.toDtoList(parameters);

        // Filtrar los que tengan código con "TIPO_PARAMETRO"
        List<Parameter> typesParameter = repository.getListParameterByCode("TIPO_PARAMETRO");
        List<ParameterDto> typesParameterDto = typesParameter.stream()
                .filter(p -> p.getCode() != null && p.getCode().contains("TIPO_PARAMETRO"))
                .map(mapper::toDto)
                .toList();

        // Construimos la respuesta
        return ParameterViewResponse.builder()
                .parameters(parameterDtos)
                .typesParameter(typesParameterDto)
                .totalParameters(total)
                .activeParameters(activeParameters)
                .inactiveParameters(inactiveParameters)
                .parametersWithParent(parametersWithParent)
                .build();
    }

    @Override
    public ParameterFormResponse initFormData(ParameterFormRequest request) {
        ParameterDto parameter = null;

        if (request.getId() != null) {
            parameter = mapper.toDto(repository.getParameterById(request.getId()));
        }

        List<ParameterDto> types = mapper.toDtoList(repository.getAllListParameterByCode("TIPO_PARAMETRO"));

        return ParameterFormResponse.builder()
                .parameter(parameter)
                .types(types)
                .build();
    }



    @Override
    public ParameterDto getParameterById(Long id) {
        return mapper.toDto(repository.getParameterById(id));
    }

    @Override
    public Parameter create(ParameterCreateRequest request) {
        Parameter parameter = mapper.toDomain(request);

        boolean enableParameter = repository.existsActiveAndNotDeletedParameter(request.getType(), "TIPO_PARAMETRO");

        if (!enableParameter) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El tipo de parámetro no esta disponible.");
        }
        // Validación y guardado de archivo si el tipo es 1
        if (request.getType() == 1) {
            if (request.getFile() == null || request.getFile().isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe enviar un archivo cuando el tipo es archivo.");
            }
            String fileUrl = handleFileUpload(request.getFile(), parameter.getId(), null);
            parameter.setName(fileUrl);
        }

        return repository.save(parameter);
    }

    @Override
    public Parameter update(ParameterUpdateRequest request) {
        Parameter existing = repository.getParameterById(request.getId());

        if(!Objects.equals(existing.getType(), request.getType())) {
            boolean enableParameter = repository.existsActiveAndNotDeletedParameter(request.getType(), "TIPO_PARAMETRO");

            if (!enableParameter) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "El tipo de parámetro no esta disponible.");
            }
        }
        Parameter parameter = mapper.toDomain(request);

        // Manejo especial si es tipo 1
        if (request.getType() == 1) {
            MultipartFile newFile = request.getFile();

            // Caso: envió un archivo nuevo
            if (newFile != null && !newFile.isEmpty()) {
                String fileUrl = handleFileUpload(newFile, parameter.getId(), existing.getName());
                parameter.setName(fileUrl);
            } else {
                // Caso: no envió archivo nuevo → mantener ruta anterior
                parameter.setName(existing.getName());
            }

        } else {
            // Si no es tipo 1, no tocar la ruta de archivo
            parameter.setName(existing.getName());
        }

        return repository.update(parameter);
    }

    /**
     * Maneja la subida de archivos de parámetros (tipo 1).
     */
    private String handleFileUpload(MultipartFile file, Long parameterId, String oldUrl) {
        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir, "uploads", "parameters");
            java.nio.file.Files.createDirectories(uploadPath);

            // Eliminar archivo anterior si existía
            if (oldUrl != null && !oldUrl.isEmpty()) {
                String oldFileName = oldUrl.substring(oldUrl.lastIndexOf("/") + 1);
                java.nio.file.Path oldFilePath = uploadPath.resolve(oldFileName);
                if (java.nio.file.Files.exists(oldFilePath)) {
                    java.nio.file.Files.delete(oldFilePath);
                }
            }

            // Generar nuevo nombre único de archivo
            String extension = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = "archive_" + System.currentTimeMillis() + extension;
            java.nio.file.Path newPath = uploadPath.resolve(fileName);

            // Guardar archivo en disco
            file.transferTo(newPath.toFile());

            return backendUrl + "/uploads/parameters/" + fileName;

        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al subir archivo: " + e.getMessage());
        }
    }



    @Override
    public boolean delete(Long id) {
        return repository.delete(id);
    }

    @Override
    public Boolean updateStatus(Long id) {
        return repository.updateStatus(id);
    }

    @Override
    public List<Parameter> getListParameterByCode(String code) {
        return repository.getListParameterByCode(code);
    }

    @Override
    public byte[] getFileAsBytes(String filename) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, "uploads", "parameters", filename);

            if (!java.nio.file.Files.exists(filePath)) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Archivo no encontrado: " + filename);
            }

            return java.nio.file.Files.readAllBytes(filePath);

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Error leyendo archivo: " + e.getMessage());
        }
    }

}
