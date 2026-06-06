package com.example.recrutement.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferenceDTO {
    private boolean enabledInApp;
    private boolean enabledEmail;
    private boolean dailyBriefing;
    private int iaMatchingThreshold;
    private int interviewReminder24h;
    private int interviewReminder2h;
    private int interviewReminder30min;
    private String[] disabledTypes;
}