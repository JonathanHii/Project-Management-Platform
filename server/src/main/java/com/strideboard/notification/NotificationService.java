package com.strideboard.notification;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.strideboard.data.notification.InboxItem;
import com.strideboard.data.notification.Notification;
import com.strideboard.data.notification.NotificationRepository;
import com.strideboard.data.notification.NotificationType;
import com.strideboard.data.workspace.Membership;
import com.strideboard.data.workspace.MembershipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public List<InboxItem> getUserNotifications(UUID userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);

        return notifications.stream().map(n -> {
            String projectName = null;
            if (n.getWorkItem() != null && n.getWorkItem().getProject() != null) {
                projectName = n.getWorkItem().getProject().getName();
            }

            return InboxItem.builder()
                    .id(n.getId())
                    .type(n.getType().name().toLowerCase())
                    .workspaceName(n.getWorkspace().getName())
                    .projectName(projectName)
                    .subtitle(n.getSubtitle())
                    .time(n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .build();
        })
                .collect(Collectors.toList());
    }

    public void markAsRead(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void acceptInvite(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getType() != NotificationType.INVITE) {
            throw new RuntimeException("Cannot accept a notification that is not an invite");
        }

        // Add User to Workspace
        boolean alreadyMember = membershipRepository.existsByUserIdAndWorkspaceId(
                notification.getRecipient().getId(),
                notification.getWorkspace().getId());

        if (!alreadyMember) {
            Membership newMembership = Membership.builder()
                    .user(notification.getRecipient())
                    .workspace(notification.getWorkspace())
                    .role("MEMBER")
                    .build();

            membershipRepository.save(newMembership);
        }

        // Delete the notification
        notificationRepository.delete(notification);
    }

    public void rejectInvite(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
