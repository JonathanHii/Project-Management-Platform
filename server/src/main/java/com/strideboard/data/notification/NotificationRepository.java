package com.strideboard.data.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // Fetch notifications for a specific user, ordered by most recent
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    boolean existsByRecipientIdAndWorkspaceIdAndType(UUID recipientId, UUID workspaceId, NotificationType type);
}
