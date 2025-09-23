package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.parameter.ParameterCreateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterFormRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterUpdateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterViewRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterFormResponse;
import com.sistema.sistema.application.dto.response.parameter.ParameterViewResponse;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.repository.ParameterRepository;
import com.sistema.sistema.domain.usecase.ParameterUseCase;
import com.sistema.sistema.infrastructure.persistence.parameter.ParameterMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ParameterService implements ParameterUseCase {

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
                .filter(p -> p.getActive() != null && p.getActive())
                .count();

        // Parámetros inactivos
        Long inactiveParameters = total - activeParameters;

        // Parámetros que tienen parentParameterId
        Long parametersWithParent = parameters.stream()
                .filter(p -> p.getParentParameterId() != null)
                .count();

        // Construimos la respuesta
        return ParameterViewResponse.builder()
                .parameters(parameters)
                .totalParameters(total)
                .activeParameters(activeParameters)
                .inactiveParameters(inactiveParameters)
                .parametersWithParent(parametersWithParent)
                .build();
    }

    @Override
    public ParameterFormResponse initFormData(ParameterFormRequest request) {
        Parameter parameter = null;

        if (request.getId() != null) {
            parameter = repository.getParameterById(request.getId());
        }

        List<Parameter> types = repository.getListParameterByCode("TIPO_PARAMETRO");

        return ParameterFormResponse.builder()
                .parameter(parameter)
                .types(types)
                .build();
    }



    @Override
    public Parameter getParameterById(Long id) {
        return repository.getParameterById(id);
    }

    @Override
    public Parameter create(ParameterCreateRequest request) {
        Parameter parameter = mapper.toDomain(request);
        return repository.save(parameter);
    }

    @Override
    public Parameter update(ParameterUpdateRequest request) {
        Parameter parameter = mapper.toDomain(request);
        return repository.update(parameter);
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
}
