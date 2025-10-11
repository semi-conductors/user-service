package com.rentmate.service.user.controller;

import com.rentmate.service.user.domain.dto.PagedResponse;
import com.rentmate.service.user.domain.dto.report.ReportDetailsResponse;
import com.rentmate.service.user.domain.dto.report.ReportListResponse;
import com.rentmate.service.user.domain.dto.report.ReportResponse;
import com.rentmate.service.user.domain.dto.report.CreateReportRequest;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;
import com.rentmate.service.user.service.ReportService;
import com.rentmate.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/reports") @PreAuthorize("hasRole('DELIVERY_GUY')")
    @Operation(
            summary = "Submit a report", description = "Submit a report against another user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reportService.createReport(request));
    }

    @GetMapping("/reports/submitted")
    public ResponseEntity<ReportListResponse> getSubmittedReports(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(reportService.getSubmittedReports(UserService.getAuthenticatedUser(), page, limit));
    }

    @GetMapping("/reports/received")
    public ResponseEntity<ReportListResponse> getReceivedReports(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(reportService.getReceivedReports(UserService.getAuthenticatedUser(), page, limit));
    }

    @GetMapping("/reports") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ReportListResponse> getAllReports(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,

            @Parameter(description = "Report status" , schema = @Schema(allowableValues = {"PENDING", "UNDER_REVIEW", "RESOLVED", "DISMISSED"}))
            @RequestParam(value = "status", required = false) ReportStatus status,

            @Parameter(description = "Report type" , schema = @Schema(allowableValues = {"FRAUD", "DAMAGE", "OVERDUE", "FAKE_USER", "THIEVING"}))
            @RequestParam(value = "type" , required = false) ReportType type
    ) {
        return ResponseEntity.ok(reportService.getAllReports(page, limit, status, type));
    }

    @GetMapping("/reports/available") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ReportListResponse> getAvailableReports(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @Parameter(description = "Report type" , schema = @Schema(allowableValues = {"FRAUD", "DAMAGE", "OVERDUE", "FAKE_USER", "THIEVING"}))
            @RequestParam(value = "type" , required = false) ReportType type
    ) {
        return ResponseEntity.ok(reportService.getAllReports(page, limit, ReportStatus.PENDING,type));
    }

    @GetMapping("/reports/{id}")  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ReportDetailsResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReport(id));
    }

    @PostMapping("/reports/{id}/claim")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ReportDetailsResponse> claimReport(@PathVariable("id") Long reportId) {
        return ResponseEntity.ok(reportService.claimReport(reportId, UserService.getAuthenticatedUser()));
    }

    @PostMapping("/reports/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> releaseReport(@PathVariable("id") Long reportId) {
        reportService.releaseReport(reportId, UserService.getAuthenticatedUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{id}/refresh-lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<LocalDateTime> refreshLock(@PathVariable("id") Long reportId){
        return ResponseEntity.ok(reportService.refreshLock(reportId, UserService.getAuthenticatedUser()));
    }

    @PostMapping("/reports/{id}/resolve") @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> resolve(@PathVariable("id") Long reportId){
        reportService.resolveReport(reportId, UserService.getAuthenticatedUser(), false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{id}/dismiss") @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> dismissReport(@PathVariable("id") Long reportId){
        reportService.resolveReport(reportId, UserService.getAuthenticatedUser(), true);
        return ResponseEntity.noContent().build();
    }
}
