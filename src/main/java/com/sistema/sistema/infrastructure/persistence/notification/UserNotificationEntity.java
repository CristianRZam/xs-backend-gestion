package com.sistema.sistema.infrastructure.persistence.notification;

import com.sistema.sistema.infrastructure.persistence.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_notifications_notification_user",
                columnNames = {"notification_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_notifications_seq_generator")
    @SequenceGenerator(
            name = "user_notifications_seq_generator",
            sequenceName = "user_notifications_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private NotificationEntity notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
