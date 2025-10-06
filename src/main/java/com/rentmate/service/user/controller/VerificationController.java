package com.rentmate.service.user.controller;

import com.rentmate.service.user.domain.dto.verification.*;
import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;
import com.rentmate.service.user.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/users/verifications")
@RequiredArgsConstructor
@Tag(name = "Identity Verification", description = "Endpoints for managing identity verification workflow. " +
        "Users can submit verification requests with ID documents, and admins can review and approve/reject them. " +
        "Includes cooldown mechanism to prevent spam submissions after rejection.")
public class VerificationController {
    private final VerificationService verificationService;

    @GetMapping("/upload-urls")
    @Operation(
            summary = "Generate pre-signed Cloudinary upload URLs",
            description = "Generates secure, time-limited pre-signed URLs for uploading ID document images (front and back) to Cloudinary. " +
                    "Returns separate upload parameters for both front and back images. " +
                    "These URLs should be used to upload images directly to Cloudinary before submitting the verification request. " +
                    "Each URL includes: api_key, timestamp, signature, public_id, and upload endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Upload URLs generated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = "{\"front\":{\"api_key\":\"...\",\"timestamp\":1234567890,\"signature\":\"...\",\"public_id\":\"id_front_uuid\"}," +
                                            "\"back\":{\"api_key\":\"...\",\"timestamp\":1234567890,\"signature\":\"...\",\"public_id\":\"id_back_uuid\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> getUploadUrls() {
        return ResponseEntity.ok(verificationService.getUploadUrls());
    }

    @GetMapping("/can-submit")
    @Operation(
            summary = "Check if user can submit verification request",
            description = "Validates whether the authenticated user is eligible to submit a new verification request. " +
                    "Returns false if: " +
                    "1) User already has a PENDING or APPROVED verification request, OR " +
                    "2) User's last request was REJECTED and the 48-hour cooldown period hasn't elapsed. " +
                    "Use this endpoint before allowing users to start the verification process to provide clear feedback.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Eligibility check completed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CanSubmitResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<CanSubmitResponse> canSubmit() {
        return ResponseEntity.ok(verificationService.canSubmit());
    }

    @PostMapping
    @Operation(
            summary = "Submit identity verification request",
            description = "Creates a new identity verification request with uploaded ID document URLs and ID number. " +
                    "User must first upload images to Cloudinary using the URLs from /upload-urls endpoint. " +
                    "Request is created with PENDING status and awaits admin review. " +
                    "Validation checks: " +
                    "- User doesn't have existing PENDING/APPROVED request " +
                    "- 48-hour cooldown period has passed since last rejection " +
                    "- ID number is exactly 14 digits " +
                    "- Image URLs are valid Cloudinary URLs",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Verification request created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VerificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Cannot submit - existing request or cooldown period active",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<VerificationResponse> createVerification(
            @Parameter(description = "Verification request with ID document URLs and ID number", required = true)
            @Valid @RequestBody CreateVerificationRequest request
    ) {
        var verification = verificationService.createVerification(request);
        return ResponseEntity.created(URI.create("/users/verifications/" + verification.id())).body(verification);
    }

    @GetMapping("/my-requests")
    @Operation(
            summary = "Get all verification requests for authenticated user",
            description = "Retrieves the complete history of all verification requests submitted by the authenticated user, " +
                    "including PENDING, APPROVED, and REJECTED requests. " +
                    "Useful for users to track their verification status and history. " +
                    "Returns requests sorted by creation date (most recent first).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User's verification requests retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Iterable.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<Iterable<VerificationResponse>> getMyRequests() {
        return ResponseEntity.ok(verificationService.getCurrentUserVerifications());
    }

    @GetMapping("/{verificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Get detailed verification request (Admin/Manager only)",
            description = "Retrieves complete details of a specific verification request including: " +
                    "- ID document image URLs (with time-limited read access) " +
                    "- ID number and verification status " +
                    "- Submitter's profile information " +
                    "- Reviewer's profile information (if reviewed) " +
                    "- Submission and review timestamps " +
                    "Used by admins to review and make approval/rejection decisions. Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Verification details retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VerificationDetailsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Verification request not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<VerificationDetailsResponse> getVerification(
            @Parameter(description = "Verification request ID to retrieve", required = true, example = "123")
            @PathVariable Long verificationId
    ) {
        return ResponseEntity.ok(verificationService.getVerification(verificationId));
    }

    @PatchMapping("/{verificationId}/approval")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Approve verification request (Admin/Manager only)",
            description = "Approves a pending identity verification request and grants the user verified status. " +
                    "Actions performed: " +
                    "1) Sets verification_requests.status = APPROVED " +
                    "2) Updates users.is_identity_verified = TRUE " +
                    "3) Records reviewer ID and review timestamp " +
                    "4) Clears any rejection reason " +
                    "5) Publishes IdentityVerificationApprovedEvent for notification service " +
                    "Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Verification request approved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VerificationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Verification request not found",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<VerificationResponse> approveVerification(
            @Parameter(description = "Verification request ID to approve", required = true, example = "123")
            @PathVariable Long verificationId
    ) {
        return ResponseEntity.ok(verificationService.approveVerification(verificationId));
    }

    @PatchMapping("/{verificationId}/rejection")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Reject verification request (Admin/Manager only)",
            description = "Rejects a pending identity verification request with a mandatory rejection reason. " +
                    "Actions performed: " +
                    "1) Sets verification_requests.status = REJECTED " +
                    "2) Stores the rejection reason for user feedback " +
                    "3) Sets can_resubmit_after to current time + 48 hours (cooldown period) " +
                    "4) Records reviewer ID and review timestamp " +
                    "5) Publishes IdentityVerificationRejectedEvent for notification service " +
                    "User cannot submit a new request until the cooldown period expires. Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Verification request rejected successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VerificationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "Missing or invalid rejection reason",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Verification request not found",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<VerificationResponse> rejectVerification(
            @Parameter(description = "Verification request ID to reject", required = true)
            @PathVariable Long verificationId,
            @Parameter(description = "Reason for rejection (will be shown to user)")
            @RequestParam(defaultValue = "no reason mentioned") String reason
    ) {
        return ResponseEntity.ok(verificationService.rejectVerification(verificationId, reason));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "List all verification requests with filtering (Admin/Manager only)",
            description = "Retrieves a paginated list of all verification requests in the system. " +
                    "Supports filtering by status (PENDING, APPROVED, REJECTED) to prioritize review workload. " +
                    "Results can be sorted by creation date, review date, or ID. " +
                    "Useful for admins to manage the verification queue and track review metrics. " +
                    "Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Verification requests retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VerificationListResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<VerificationListResponse> getAll(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,

            @Parameter(description = "Filter by verification status", schema = @Schema(allowableValues = {"PENDING", "APPROVED", "REJECTED"}))
            @RequestParam(required = false) VerificationRequestStatus status,

            @Parameter(description = "Field to sort by",schema = @Schema(allowableValues = {"id", "createdAt", "reviewedAt"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction",schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        return ResponseEntity.ok(verificationService.getAll(page, limit, status, sortBy, sortOrder));
    }
}