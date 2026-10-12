package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.response.AppVersionResponse;
import com.sistema.sistema.infrastructure.persistence.appversion.AppVersionPolicyEntity;
import com.sistema.sistema.infrastructure.persistence.appversion.JpaAppVersionPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppVersionService {
    private final JpaAppVersionPolicyRepository repository;

    public AppVersionResponse check(String platform, String buildHeader) {
        String normalizedPlatform = platform == null ? "" : platform.trim().toUpperCase();
        int build = parseBuild(buildHeader);
        AppVersionPolicyEntity policy = repository.findByPlatformIgnoreCase(normalizedPlatform).orElse(null);
        if (policy == null) {
            log.warn("Rejected undeclared mobile platform {}", normalizedPlatform);
            return AppVersionResponse.builder().updateAvailable(true).updateRequired(true)
                    .message("Esta versión de la aplicación no está autorizada. Actualice la aplicación.").build();
        }
        Set<Integer> allowedBuilds = Arrays.stream(policy.getAllowedBuilds().split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).map(this::parseBuild)
                .collect(Collectors.toSet());
        boolean declared = build > 0 && allowedBuilds.contains(build);
        boolean updateAvailable = build < policy.getLatestBuild();
        boolean updateRequired = !declared || build < policy.getMinimumBuild();
        String message = policy.getUpdateMessage();
        if (message == null || message.isBlank()) {
            message = updateRequired ? "Debes actualizar la aplicación para continuar."
                    : updateAvailable ? "Hay una nueva versión disponible." : "La aplicación está actualizada.";
        }
        return AppVersionResponse.builder().updateAvailable(updateAvailable).updateRequired(updateRequired)
                .latestBuild(policy.getLatestBuild()).latestVersion(policy.getLatestVersion())
                .downloadUrl(policy.getDownloadUrl()).message(message).build();
    }

    private int parseBuild(String value) {
        try { return Integer.parseInt(value == null ? "" : value.trim()); }
        catch (NumberFormatException exception) { return -1; }
    }
}
