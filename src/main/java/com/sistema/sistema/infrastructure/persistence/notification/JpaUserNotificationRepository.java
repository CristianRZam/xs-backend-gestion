package com.sistema.sistema.infrastructure.persistence.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaUserNotificationRepository extends JpaRepository<UserNotificationEntity, Long> {

    List<UserNotificationEntity> findByUserIdAndDismissedAtIsNullOrderByNotificationCreatedAtDesc(Long userId);

    long countByUserIdAndReadAtIsNullAndDismissedAtIsNull(Long userId);

    Optional<UserNotificationEntity> findByNotificationIdAndUserIdAndDismissedAtIsNull(
            Long notificationId,
            Long userId
    );

    boolean existsByNotificationIdAndUserId(Long notificationId, Long userId);

    @Modifying
    @Query("""
            UPDATE UserNotificationEntity userNotification
            SET userNotification.readAt = :readAt
            WHERE userNotification.user.id = :userId
              AND userNotification.readAt IS NULL
              AND userNotification.dismissedAt IS NULL
            """)
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}
