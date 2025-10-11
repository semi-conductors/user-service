package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.UserReport;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;
import com.rentmate.service.user.domain.mapper.ReportMapper;
import com.rentmate.service.user.repository.UserReportRepository;
import com.rentmate.service.user.repository.UserRepository;
import com.rentmate.service.user.service.ReportEscalationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j
public class ReportEscalationServiceImpl implements ReportEscalationService {
    private final UserReportRepository reportRepository;
    private final UserRepository userRepository;

    @Value("${report.escalation-period-hours:72}")
    private Long escalationPeriod;

    @Override @Transactional
    public int escalateOverdueReportsToThieving() {
        List<UserReport> overdueReports = reportRepository
                .findReportsSubbmitedBefore(
                        LocalDateTime.now().minusHours(escalationPeriod),
                        ReportType.OVERDUE,
                        ReportStatus.PENDING
                );

        int escalatedReports = 0;
        for (UserReport overdueReport : overdueReports) {
            try{
                escalateReport(overdueReport);
                escalatedReports++;
            }catch (Exception e) {
                log.error("Failed to escalate overdue report", e);
            }
        }

        return escalatedReports;
    }

    private void escalateReport(UserReport overdueReport) {
        UserReport thievingReport = ReportMapper.createThievingReport(overdueReport, buildThievingDetails(overdueReport));
        reportRepository.save(thievingReport);

        overdueReport.setStatus(ReportStatus.RESOLVED);
        overdueReport.setResolvedAt(LocalDateTime.now());
        overdueReport.setResolutionNotes(String.format(
                "\n\n[SYSTEM] This report was automatically escalated to THIEVING report (ID: %d) on %s",
                thievingReport.getId(),
                LocalDateTime.now()
        ));

        reportRepository.save(overdueReport);

        suspendUser(overdueReport.getReportedUser());
    }

    private void suspendUser(User reportedUser) {
        reportedUser.setActivityStatus(AccountActivityStatus.SUSPENDED_BY_ADMIN);
        reportedUser.setDisabled(true);
        userRepository.save(reportedUser);
    }

    private String buildThievingDetails(UserReport overdueReport) {
        StringBuilder details = new StringBuilder();

        details.append("AUTO-ESCALATED THEFT REPORT\n\n");
        details.append("This report was automatically escalated from OVERDUE (Report ID: ")
                .append(overdueReport.getId())
                .append(") after 72 hours without resolution.\n\n");

        details.append("ORIGINAL OVERDUE REPORT:\n");
        details.append("Submitted: ").append(overdueReport.getSubmittedAt()).append("\n");
        details.append("Details: ").append(overdueReport.getDetails()).append("\n\n");

        details.append("ESCALATION REASON:\n");
        details.append("The renter has failed to return the item for more than 3 days ");
        details.append("after the initial overdue report. This constitutes theft.\n\n");

        details.append("REQUIRED ACTIONS:\n");
        details.append("1. Contact local authorities\n");
        details.append("2. Provide police report with full renter details\n");
        details.append("3. Initiate legal proceedings if necessary\n");
        details.append("4. Process insurance claim if applicable");

        return details.toString();
    }
}
