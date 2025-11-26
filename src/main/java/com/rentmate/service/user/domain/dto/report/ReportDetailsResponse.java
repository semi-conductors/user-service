package com.rentmate.service.user.domain.dto.report;

import com.rentmate.service.user.domain.dto.user.UserProfileResponse;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ReportDetailsResponse {
    private Long id;

    private ReportType reportType;

    private ReportStatus status;

    private String details;

    private Long relatedRentalId;

    private Long relatedDeliveryId;

    private BigDecimal damagePercentage;

    private LocalDateTime submittedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime claimedAt;

    private LocalDateTime lockExpiresAt;

    private String resolutionNotes;
    private UserProfileResponse reporter;
    private UserProfileResponse reported;
}
