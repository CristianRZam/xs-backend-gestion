package com.sistema.sistema.application.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;
    private String type;
    private String priority;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private String metadata;
    private LocalDateTime createdAt;
    private Boolean read;
    private LocalDateTime readAt;
}
