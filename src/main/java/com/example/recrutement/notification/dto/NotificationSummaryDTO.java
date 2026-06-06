// com.example.recrutement.notification.dto.NotificationSummaryDTO.java
package com.example.recrutement.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class NotificationSummaryDTO {
    private int totalUnread;
    private int totalUrgent;
    private Map<String, Integer> unreadByType;
    private List<NotificationDTO> recentNotifications;
    private Map<String, Object> dashboardWidgets;
}