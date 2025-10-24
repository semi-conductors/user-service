package com.rentmate.service.user.service;

import com.rentmate.service.user.domain.dto.PagedResponse;
import com.rentmate.service.user.domain.dto.report.ReportDetailsResponse;
import com.rentmate.service.user.domain.dto.report.ReportListResponse;
import com.rentmate.service.user.domain.dto.report.ReportResponse;
import com.rentmate.service.user.domain.dto.report.CreateReportRequest;
import com.rentmate.service.user.domain.dto.user.UserPrincipal;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;

import java.time.LocalDateTime;

public interface ReportService {
    ReportResponse createReport(CreateReportRequest request);
    ReportListResponse getSubmittedReports(UserPrincipal loggedInUser, int page, int limit);
    ReportListResponse getReceivedReports(UserPrincipal loggedInUser, int page, int limit);
    ReportListResponse getAllReports(int page, int limit, ReportStatus status, ReportType type);
    ReportDetailsResponse getReport(Long id);
    void claimReport(Long id, UserPrincipal loggedInUser);
    void releaseReport(Long id, UserPrincipal loggedInUser);
    void resolveReport(Long id, UserPrincipal loggedInUser, boolean dismissed);
    LocalDateTime refreshLock(Long reportId, UserPrincipal loggedInUser);
}