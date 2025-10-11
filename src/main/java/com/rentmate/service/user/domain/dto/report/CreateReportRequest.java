package com.rentmate.service.user.domain.dto.report;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class CreateReportRequest {
    public enum ReportType {
        FRAUD, DAMAGE, OVERDUE, FAKE_USER
    }

    @NotNull(message = "Reported user ID is required")
    private Long reportedUserId;

    @NotNull(message = "Reporter user ID is required")
    private Long reporterUserId;

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotBlank(message = "Details are required")
    @Size(min = 20, max = 2000, message = "Details must be between 20 and 2000 characters")
    private String details;
    private Long relatedRentalId;

    @NotNull(message = "Related delivery ID is required")
    private Long relatedDeliveryId;
    @DecimalMin(value = "0.0", message = "Damage percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Damage percentage must be between 0 and 100")
    private BigDecimal damagePercentage;
}

