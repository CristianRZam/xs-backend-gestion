package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.profile.PasswordRequest;
import com.sistema.sistema.application.dto.request.profile.ProfileUpdateRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.profile.ProfileDTO;
import com.sistema.sistema.application.dto.response.profile.ProfileFormresponse;
import com.sistema.sistema.application.dto.response.profile.ProfileViewResponse;
import com.sistema.sistema.domain.model.Permission;
import com.sistema.sistema.domain.usecase.ProfileUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileUseCase profileUseCase;

    public ProfileController(
            ProfileUseCase profileUseCase
    ) {
        this.profileUseCase = profileUseCase;
    }

    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/init")
    public ResponseEntity<ApiResponse<ProfileViewResponse>> initView() {
        ProfileViewResponse data = profileUseCase.init();
        return ApiResponseFactory.success(data, "Datos vista perfil cargado correctamente");
    }

    @PreAuthorize("hasAuthority('UPDATE_PROFILE')")
    @GetMapping("/init-form")
    public ResponseEntity<ApiResponse<ProfileFormresponse>> initFormData() {
        ProfileFormresponse data = profileUseCase.initFormData();
        return ApiResponseFactory.success(data, "Datos de inicialización del formulario cargados correctamente");
    }

    @PreAuthorize("hasAuthority('UPDATE_PROFILE')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ProfileDTO>> update(@Valid @RequestBody ProfileUpdateRequest request) {
        ProfileDTO data = profileUseCase.update(request);
        return ApiResponseFactory.success(data, "Usuario actualizado correctamente");
    }

    @PreAuthorize("hasAuthority('UPDATE_PROFILE')")
    @PutMapping("/update-password")
    public ResponseEntity<ApiResponse<Boolean>> updatePassword(@Valid @RequestBody PasswordRequest request) {
        Boolean data = profileUseCase.updatePassword(request);
        return ApiResponseFactory.success(data, "Usuario actualizado correctamente");
    }

    @PreAuthorize("hasAuthority('UPDATE_PROFILE')")
    @PutMapping("/disable-account")
    public ResponseEntity<ApiResponse<Boolean>> disableAccount() {
        Boolean data = profileUseCase.disableAccount();
        return ApiResponseFactory.success(data, "Usuario actualizado correctamente");
    }

    @PreAuthorize("hasAuthority('UPDATE_PROFILE')")
    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<ProfileDTO>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        ProfileDTO data = profileUseCase.uploadAvatar(file);
        return ApiResponseFactory.success(data, "Avatar subido correctamente");
    }

    @PreAuthorize("hasAuthority('UPDATE_PROFILE')")
    @DeleteMapping("/delete-avatar")
    public ResponseEntity<ApiResponse<ProfileDTO>> deleteAvatar() {
        ProfileDTO data = profileUseCase.deleteAvatar();
        return ApiResponseFactory.success(data, "Avatar eliminado correctamente");
    }

    @PreAuthorize("hasAuthority('PERMISSION_PROFILE')")
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<Permission>>> loadMyPermissions() {
        List<Permission> data = profileUseCase.loadMyPermissions();
        return ApiResponseFactory.success(data, "Mis permisos cargados correctamente.");
    }
}
