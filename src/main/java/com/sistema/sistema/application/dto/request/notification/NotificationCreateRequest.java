package com.sistema.sistema.application.dto.request.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateRequest {

    @NotBlank
    @Size(max = 160)
    private String eventKey;

    @NotBlank
    @Size(max = 50)
    private String type;

    @NotBlank
    @Size(max = 20)
    private String priority;

    @NotBlank
    @Size(max = 160)
    private String title;

    @NotBlank
    private String message;

    @Size(max = 50)
    private String referenceType;

    private Long referenceId;

    private String metadata;
}
