package com.rentmate.service.user.domain.dto.report;

import com.rentmate.service.user.domain.enumuration.ReportType;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;

    private ReportType reportType;

    private ReportStatus status;

    private Long reporterId;

    private String reporterName;

    private Long reportedUserId;

    private String reportedUserName;

    private String details;

    private Long relatedRentalId;

    private Long relatedDeliveryId;

    private BigDecimal damagePercentage;

    private LocalDateTime submittedAt;

    private LocalDateTime resolvedAt;
}
