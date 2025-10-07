package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.parameter.ParameterCreateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterFormRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterUpdateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterViewRequest;
import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.parameter.ParameterFormResponse;
import com.sistema.sistema.application.dto.response.parameter.ParameterViewResponse;
import com.sistema.sistema.domain.model.Parameter;
import jakarta.validation.Valid;

import java.util.List;

public interface ParameterUseCase {
    ParameterViewResponse init(ParameterViewRequest request);
    ParameterFormResponse initFormData(ParameterFormRequest request);
    ParameterDto getParameterById(Long id);
    Parameter create(@Valid ParameterCreateRequest request);
    Parameter update(@Valid ParameterUpdateRequest request);
    boolean delete(Long id);
    Boolean updateStatus(Long id);
    List<Parameter> getListParameterByCode(String code);
    byte[] getFileAsBytes(String filename);
}
