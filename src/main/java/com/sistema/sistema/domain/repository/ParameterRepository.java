package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.parameter.ParameterViewRequest;
import com.sistema.sistema.domain.model.Parameter;

import java.util.List;

public interface ParameterRepository {
    List<Parameter> findByDeletedAtIsNull(ParameterViewRequest request);
    Parameter getParameterById(Long id);
    List<Parameter> getListParameterByCode(String code);
    Parameter save(Parameter parameter);
    Parameter update(Parameter parameter);
    boolean delete(Long id);
    Boolean updateStatus(Long id);
}
