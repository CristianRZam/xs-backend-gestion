package com.sistema.sistema.infrastructure.persistence.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Optional<NotificationEntity> findByEventKey(String eventKey);
}
