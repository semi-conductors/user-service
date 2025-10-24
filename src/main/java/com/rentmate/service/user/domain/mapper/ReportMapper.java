package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.rating.CreateRatingRequest;
import com.rentmate.service.user.domain.dto.report.ReportDetailsResponse;
import com.rentmate.service.user.domain.dto.report.ReportResponse;
import com.rentmate.service.user.domain.dto.report.CreateReportRequest;
import com.rentmate.service.user.domain.dto.user.UserPrincipal;
import com.rentmate.service.user.domain.dto.user.UserProfileResponse;
import com.rentmate.service.user.domain.dto.user.UsernameDto;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.UserReport;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;
import org.springframework.boot.autoconfigure.reactor.ReactorProperties;

import java.time.LocalDateTime;

public class ReportMapper {
    public static ReportResponse toReportResponse(UserReport report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reportType(report.getReportType())
                .status(report.getStatus())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getFirstName() + " " + report.getReporter().getLastName())
                .reportedUserId(report.getReportedUser().getId())
                .reportedUserName(report.getReportedUser().getFirstName() + " " + report.getReportedUser().getLastName())
                .details(report.getDetails())
                .relatedRentalId(report.getRelatedRentalId())
                .relatedDeliveryId(report.getRelatedDeliveryId())
                .damagePercentage(report.getDamagePercentage())
                .submittedAt(report.getSubmittedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }

    public static ReportResponse toReportResponse(UserReport report, UsernameDto reporter, UsernameDto reportedUser) {
        return ReportResponse.builder()
                .id(report.getId())
                .reportType(report.getReportType())
                .status(report.getStatus())
                .reporterId(reporter.getId())
                .reporterName(reporter.getFirstName() + " " + reporter.getLastName())
                .reportedUserId(reportedUser.getId())
                .reportedUserName(reportedUser.getFirstName() +" " + reportedUser.getLastName())
                .details(report.getDetails())
                .relatedRentalId(report.getRelatedRentalId())
                .relatedDeliveryId(report.getRelatedDeliveryId())
                .damagePercentage(report.getDamagePercentage())
                .submittedAt(report.getSubmittedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }

    public static UserReport toUserReport(CreateReportRequest request, User reporter, User reportedUser) {
        UserReport report = new UserReport();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReportType(Enum.valueOf(ReportType.class, request.getReportType().name()));
        report.setDetails(request.getDetails());
        report.setRelatedRentalId(request.getRelatedRentalId());
        report.setRelatedDeliveryId(request.getRelatedDeliveryId());
        report.setDamagePercentage(request.getDamagePercentage());
        report.setStatus(ReportStatus.PENDING);
        report.setSubmittedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        return report;
    }

    public static UserReport createThievingReport(UserReport overdueReport, String details) {
        UserReport thievingReport = new UserReport();

        thievingReport.setReporter(overdueReport.getReporter());
        thievingReport.setReportedUser(overdueReport.getReportedUser());
        thievingReport.setReportType(ReportType.THIEVING);
        thievingReport.setStatus(ReportStatus.PENDING);
        thievingReport.setRelatedRentalId(overdueReport.getRelatedRentalId());
        thievingReport.setRelatedDeliveryId(overdueReport.getRelatedDeliveryId());
        thievingReport.setSubmittedAt(LocalDateTime.now());
        thievingReport.setUpdatedAt(LocalDateTime.now());
        thievingReport.setAutoEscalatedFrom(overdueReport);

        thievingReport.setDetails(details);
        return thievingReport;
    }

    public static ReportDetailsResponse toReportDetailsResponse(UserReport report, UserProfileResponse reporter, UserProfileResponse reported) {
        return ReportDetailsResponse.builder()
                .id(report.getId())
                .reportType(report.getReportType())
                .status(report.getStatus())
                .damagePercentage(report.getDamagePercentage())
                .details(report.getDetails())
                .relatedDeliveryId(report.getRelatedDeliveryId())
                .relatedRentalId(report.getRelatedRentalId())
                .submittedAt(report.getSubmittedAt())
                .resolvedAt(report.getResolvedAt())
                .reporter(reporter)
                .reported(reported)
                .claimedAt(report.getClaimedAt())
                .lockExpiresAt(report.getLockExpiresAt()).build();
    }
}
