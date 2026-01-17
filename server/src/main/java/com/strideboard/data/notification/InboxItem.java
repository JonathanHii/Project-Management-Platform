package com.strideboard.data.notification;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InboxItem {
    private UUID id;
    private String type; // "invite" or "update"
    private String workspaceName;
    private String projectName; // can be null for invites
    private String subtitle;
    private String time; 
}


