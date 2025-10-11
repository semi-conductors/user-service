package com.rentmate.service.user.domain.dto.event;

import com.rentmate.service.user.domain.enumuration.ReportType;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ReportCreatedEvent {
    private Long reportId;
    private Long reporterId;
    private Long reportedUserId;
    private String reportedUserEmail;
    private String reportType;
}
