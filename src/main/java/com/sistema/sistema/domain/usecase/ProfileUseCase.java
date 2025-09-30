package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.profile.PasswordRequest;
import com.sistema.sistema.application.dto.request.profile.ProfileUpdateRequest;
import com.sistema.sistema.application.dto.response.profile.ProfileDTO;
import com.sistema.sistema.application.dto.response.profile.ProfileFormresponse;
import com.sistema.sistema.application.dto.response.profile.ProfileViewResponse;
import com.sistema.sistema.domain.model.Permission;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileUseCase {
    ProfileViewResponse init();
    ProfileFormresponse initFormData();
    ProfileDTO update(@Valid ProfileUpdateRequest request);

    Boolean updatePassword(@Valid PasswordRequest request);

    Boolean disableAccount();

    ProfileDTO uploadAvatar(MultipartFile file);

    ProfileDTO deleteAvatar();

    List<Permission> loadMyPermissions();
}
