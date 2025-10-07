package com.sistema.sistema.infrastructure.persistence.parameter;

import com.sistema.sistema.application.dto.request.parameter.ParameterCreateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterUpdateRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.domain.model.Parameter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParameterMapper {

    /** Convert Domain -> Entity */
    public ParameterEntity toEntity(Parameter parameter) {
        if (parameter == null) return null;

        return ParameterEntity.builder()
                .id(parameter.getId())
                .parentParameterId(parameter.getParentParameterId())
                .parameterId(parameter.getParameterId())
                .code(parameter.getCode())
                .type(parameter.getType())
                .name(parameter.getName())
                .shortName(parameter.getShortName())
                .orderNumber(parameter.getOrderNumber())
                .active(parameter.getActive())
                .createdAt(parameter.getCreatedAt())
                .createdBy(parameter.getCreatedBy())
                .modifiedAt(parameter.getModifiedAt())
                .modifiedBy(parameter.getModifiedBy())
                .deletedAt(parameter.getDeletedAt())
                .deletedBy(parameter.getDeletedBy())
                .build();
    }

    /** Convert Entity -> Domain */
    public Parameter toDomain(ParameterEntity entity) {
        if (entity == null) return null;

        return Parameter.builder()
                .id(entity.getId())
                .parentParameterId(entity.getParentParameterId())
                .parameterId(entity.getParameterId())
                .code(entity.getCode())
                .type(entity.getType())
                .name(entity.getName())
                .shortName(entity.getShortName())
                .orderNumber(entity.getOrderNumber())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .modifiedAt(entity.getModifiedAt())
                .modifiedBy(entity.getModifiedBy())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .build();
    }

    /** Convert CreateRequest -> Domain */
    public Parameter toDomain(ParameterCreateRequest request) {
        if (request == null) return null;

        return Parameter.builder()
                .code(request.getCode())
                .name(request.getName())
                .shortName(request.getShortName())
                .parentParameterId(request.getParentParameterId())
                .parameterId(request.getParameterId())
                .type(request.getType())
                .orderNumber(request.getOrderNumber())
                .active(true) // por defecto al crear
                .build();
    }

    /** Convert UpdateRequest -> Domain */
    public Parameter toDomain(ParameterUpdateRequest request) {
        if (request == null) return null;

        return Parameter.builder()
                .id(request.getId())
                .name(request.getName())
                .shortName(request.getShortName())
                .parentParameterId(request.getParentParameterId())
                .parameterId(request.getParameterId())
                .type(request.getType())
                .orderNumber(request.getOrderNumber())
                .build();
    }

    /** Convert Domain -> DTO */
    public ParameterDto toDto(Parameter param) {
        if (param == null) return null;

        String name = param.getName();
        String typeName;

        if (param.getType() != null && param.getType() == 1 && name != null) {
            // Si es archivo, recorto el nombre
            name = name.substring(name.lastIndexOf("/") + 1);
        }

        // Asignar typeName
        if (param.getType() == null) {
            typeName = "Indefinido";
        } else {
            switch (param.getType().intValue()) {
                case 1 -> typeName = "Archivo";
                case 2 -> typeName = "Texto";
                default -> typeName = "Indefinido";
            }
        }

        return ParameterDto.builder()
                .id(param.getId())
                .parentParameterId(param.getParentParameterId())
                .parameterId(param.getParameterId())
                .code(param.getCode())
                .type(param.getType())
                .typeName(typeName)
                .name(name)
                .shortName(param.getShortName())
                .orderNumber(param.getOrderNumber())
                .active(Boolean.TRUE.equals(param.getActive()))
                .deleted(param.getDeletedAt() != null)
                .build();
    }



    /** Convert List<Domain> -> List<Entity> */
    public List<ParameterEntity> toEntityList(List<Parameter> parameters) {
        return parameters.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /** Convert List<Entity> -> List<Domain> */
    public List<Parameter> toDomainList(List<ParameterEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /** Convert List<Domain> -> List<DTO> */
    public List<ParameterDto> toDtoList(List<Parameter> parameters) {
        return parameters != null
                ? parameters.stream().map(this::toDto).collect(Collectors.toList())
                : List.of();
    }
}
