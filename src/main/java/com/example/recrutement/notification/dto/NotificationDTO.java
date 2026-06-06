// com.example.recrutement.notification.dto.NotificationDTO.java
package com.example.recrutement.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String actionUrl;
    private boolean isRead;
    private boolean isArchived;
    private String priority;
    private String relatedEntityType;
    private Long relatedEntityId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}