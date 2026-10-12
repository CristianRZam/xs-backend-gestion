package com.sistema.sistema.infrastructure.persistence.appversion;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_version_policies")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AppVersionPolicyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String platform;
    @Column(name = "latest_build", nullable = false)
    private Integer latestBuild;
    @Column(name = "latest_version", nullable = false, length = 40)
    private String latestVersion;
    @Column(name = "allowed_builds", nullable = false, length = 500)
    private String allowedBuilds;
    @Column(name = "minimum_build", nullable = false)
    private Integer minimumBuild;
    @Column(name = "download_url", length = 500)
    private String downloadUrl;
    @Column(name = "update_message", length = 500)
    private String updateMessage;
}
