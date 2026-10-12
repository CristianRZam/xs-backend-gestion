package com.sistema.sistema.infrastructure.persistence.appversion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaAppVersionPolicyRepository extends JpaRepository<AppVersionPolicyEntity, Long> {
    Optional<AppVersionPolicyEntity> findByPlatformIgnoreCase(String platform);
}
