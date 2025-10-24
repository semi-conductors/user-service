package com.rentmate.service.user.controller;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Reports",
        description = "Report management system for handling user complaints, fraud detection, and dispute resolution. " +
                "Supports different report types with role-based submission and admin resolution workflows."
)
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/reports") @PreAuthorize("hasRole('DELIVERY_GUY')")
    @Operation(
            summary = "Submit a new report",
            description = """
            **Delivery Guy Only**
            
            Submit a report against another user involved in a delivery. Supported report types:
            - **FAKE_USER**: User didn't show up at delivery location
            - **FRAUD**: Item delivered is different/broken from listing
            - **OVERDUE**: Renter cannot be contacted for item return (auto-escalates to THIEVING after 72 hours)
            - **DAMAGE**: Item returned with damage (requires owner verification code)
            - **THIEVING**: Auto-generated from OVERDUE reports or manually created in severe cases
         
            **Business Rules:**
            - Only Delivery Guys can submit reports
            - reported_user_id must be associated with the delivery
            - Duplicate reports for same delivery are prevented
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Report submitted successfully")
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reportService.createReport(request));
    }

    @GetMapping("/reports/submitted")
    @Operation(
            summary = "Get reports I submitted",
            description = """
            Retrieve paginated list of all reports submitted by the authenticated user.
            
            **Access:** All authenticated users
         
            **Use Cases:**
            - Track my submitted reports
            - Monitor resolution progress
            - View report history
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReportListResponse> getSubmittedReports(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(reportService.getSubmittedReports(UserService.getAuthenticatedUser(), page, limit));
    }

    @GetMapping("/reports/received")
    @Operation(
            summary = "Get reports filed against me",
            description = """
            Retrieve paginated list of all reports filed against the authenticated user.
            
            **Access:** All authenticated users

            **Use Cases:**
            - Monitor reports against me
            - Understand account status changes
            - Prepare responses or appeals
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReportListResponse> getReceivedReports(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(reportService.getReceivedReports(UserService.getAuthenticatedUser(), page, limit));
    }

    @GetMapping("/reports") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Get all reports (Admin/Manager)",
            description = """
            **Admin/Manager Only**
            
            Retrieve paginated list of all reports with advanced filtering capabilities.
            
            **Use Cases:**
            - Monitor all platform reports
            - Filter by specific criteria
            - Track resolution metrics
            - Identify patterns and trends
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
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
    @Operation(
            summary = "Get claimable reports (Admin/Manager)",
            description = """
            **Admin/Manager Only**
            
            Retrieve paginated list of reports available for claiming and review.
            
            **Availability Criteria:**
            - Status is PENDING
            - Not currently locked by another admin
            - Or lock has expired (auto-released after 30 minutes)
 
            **Workflow:**
            1. View available reports in dashboard
            2. Click to claim a report
            3. Review and resolve within 30 minutes (or refresh lock)
            4. Complete resolution or dismiss
            
            **Use Cases:**
            - Admin dashboard work queue
            - Priority-based triage
            - Load balancing across team
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
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
    @Operation(
            summary = "Get detailed report information (Admin/Manager)",
            description = """
            **Admin/Manager Only**
            
            Retrieve complete details for a specific report including:
            
            **Use Cases:**
            - Detailed report review before claiming
            - Investigation and evidence analysis
            - Resolution decision making
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ReportDetailsResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReport(id));
    }

    @PostMapping("/reports/{id}/claim")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Claim report for review",
            description = """
            **Admin/Manager Only**
            
            Claim a report to start working on it. This action:
            
            1. **Locks the report** to prevent others from editing
            2. **Changes status** from PENDING to UNDER_REVIEW
            3. **Sets 30-minute timer** - lock auto-expires if inactive
            4. **Records ownership** - tracks who is working on it
            
            **Lock Behavior:**
            - Lock expires after 30 minutes of inactivity
            - Can be refreshed while actively working
            - Automatically released on resolve/dismiss
            - Prevents duplicate work by multiple admins
            
            **Error Conditions:**
            - Report already claimed by another admin
            - Report not in PENDING status
            - Lock still active from previous claim
            
            **Use Cases:**
            - Start reviewing a report
            - Take ownership of investigation
            - Prevent conflicts with other admins
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> claimReport(@PathVariable("id") Long reportId) {
        reportService.claimReport(reportId, UserService.getAuthenticatedUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Release report lock",
            description = """
            **Admin/Manager Only**
            
            Release your lock on a report without resolving it.
            
            **When to Use:**
            - Need to take a break
            - Report requires additional information
            - Passing to another admin
            - Cannot complete within 30 minutes
            
            **Effect:**
            - Removes lock (clears claimed_by, lock_expires_at)
            - Changes status back to PENDING
            - Report returns to available queue
            - Keeps assignment history for accountability
            
            **Note:** Lock is automatically released after 30 minutes of inactivity,
            but manual release is recommended for better team coordination.
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Report lock released successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You don't have a lock on this report"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<Void> releaseReport(@PathVariable("id") Long reportId) {
        reportService.releaseReport(reportId, UserService.getAuthenticatedUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{id}/refresh-lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Refresh report lock (keep-alive)",
            description = """
            **Admin/Manager Only**
            
            Extend the lock on a report you're actively working on.
            
            **Purpose:**
            - Prevent automatic lock expiration
            - Keep working on complex reports
            - Signal active work to other admins
            
            **Behavior:**
            - Extends lock by 30 minutes from now
            - Must be called before lock expires
            - Only works if you currently hold the lock
            
            **Recommended Usage:**
            - Call every 25 minutes for long reviews
            - Frontend should auto-refresh while admin is active
            - Helps differentiate active work from abandoned reports
            
            **Example Frontend Implementation:**
            ```javascript
            // Refresh every 25 minutes
            setInterval(() => {
              refreshLock(reportId);
            }, 25 * 60 * 1000);
            ```
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<LocalDateTime> refreshLock(@PathVariable("id") Long reportId){
        return ResponseEntity.ok(reportService.refreshLock(reportId, UserService.getAuthenticatedUser()));
    }

    @PostMapping("/reports/{id}/resolve") @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Resolve report",
            description = """
            **Admin/Manager Only**
            
            Mark report as RESOLVED after investigation and action.
            
            **Actions Performed:**
            1. Updates report status to RESOLVED
            2. Records resolution timestamp and admin
            3. Releases lock automatically
            4. Sends notifications to all parties
            
            **For FRAUD/DAMAGE Reports:**
            - Triggers deposit transfer process (via Payment Service)
            - Calculates transfer amount based on damage_percentage
            - Creates financial transaction audit trail
            - Updates rental status
            
            **Use Cases:**
            - Confirmed fraud - initiate deposit transfer
            - Damage verified - process claim
            - Issue resolved - close report
            - Account action taken - document outcome
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> resolve(@PathVariable("id") Long reportId){
        reportService.resolveReport(reportId, UserService.getAuthenticatedUser(), false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{id}/dismiss") @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Dismiss report",
            description = """
            **Admin/Manager Only**
            
            Dismiss report as unfounded or invalid.
            
            **Prerequisites:**
            - Must have active lock on the report
            - Report must be in UNDER_REVIEW status
            - Dismissal requires detailed explanation
            
            **Actions Performed:**
            1. Updates report status to DISMISSED
            2. Records dismissal reason and admin
            3. Releases lock automatically
            4. Restores reported user's account status (if was PENDING_REPORT_REVIEW)
            5. Sends notifications to reporter and reported user
            
            **Dismissal Reasons (Examples):**
            - Insufficient evidence
            - Misunderstanding or miscommunication
            - Technical issue (not user fault)
            - Duplicate report
            
            **Important:**
            - Dismissal doesn't mean reporter was malicious
            - Document reasoning thoroughly
            - Consider pattern detection for false reporters
            - Reported user account status restored to ACTIVE
            
            **Use Cases:**
            - Lack of evidence to support claim
            - Situation resolved outside system
            - Report was mistake or misunderstanding
            - No platform policy violation found
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> dismissReport(@PathVariable("id") Long reportId){
        reportService.resolveReport(reportId, UserService.getAuthenticatedUser(), true);
        return ResponseEntity.noContent().build();
    }
}
