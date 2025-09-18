package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.user.UserCreateRequest;
import com.sistema.sistema.application.dto.request.user.UserFormRequest;
import com.sistema.sistema.application.dto.request.user.UserUpdateRequest;
import com.sistema.sistema.application.dto.request.user.UserViewRequest;
import com.sistema.sistema.application.dto.response.user.UserFormResponse;
import com.sistema.sistema.application.dto.response.user.UserViewResponse;
import com.sistema.sistema.domain.model.User;
import jakarta.validation.Valid;

public interface UserUseCase {
    UserViewResponse init(UserViewRequest request);
    UserFormResponse initFormData(UserFormRequest request);
    User getUserById(Long id);
    User create(@Valid UserCreateRequest request);
    User update(@Valid UserUpdateRequest request);
    Boolean delete(Long id);
    Boolean updateStatus(Long id);
}
