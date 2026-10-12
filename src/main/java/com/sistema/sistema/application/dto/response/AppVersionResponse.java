package com.sistema.sistema.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AppVersionResponse {
    private final boolean updateAvailable;
    private final boolean updateRequired;
    private final Integer latestBuild;
    private final String latestVersion;
    private final String downloadUrl;
    private final String message;
}
