package com.rentmate.service.user.controller;

import com.rentmate.service.user.domain.dto.verification.*;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;
import com.rentmate.service.user.service.VerificationService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/users/verifications") @RequiredArgsConstructor
public class VerificationController {
    private final VerificationService verificationService;

    /// TODO: format the response into DTO
    @GetMapping("/upload-urls")
    public ResponseEntity<Map<String, Object>> getUploadUrls() {
        return ResponseEntity.ok(verificationService.getUploadUrls());
    }

    @PostMapping
    public ResponseEntity<VerificationResponse> createVerification(CreateVerificationRequest request) {
        var verification = verificationService.createVerification(request);
        return ResponseEntity.created(URI.create("" + verification.id())).body(verification);
    }

    @GetMapping("/can-submit")
    public ResponseEntity<CanSubmitResponse> canSubmit() {
        return ResponseEntity.ok(verificationService.canSubmit());
    }

    @GetMapping("/{verification_id}") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<VerificationDetailsResponse> getVerification(@PathVariable Long verification_id) {
        return ResponseEntity.ok(verificationService.getVerification(verification_id));
    }

    @PatchMapping("/{verification_id}/approval") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<VerificationResponse> approveVerification(@PathVariable Long verification_id) {
        return ResponseEntity.ok(verificationService.approveVerification(verification_id));
    }

    @PatchMapping("/{verification_id}/rejection") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<VerificationResponse> rejectVerification(@PathVariable Long verification_id,
                @RequestParam(defaultValue = "no reason mentioned") String reason) {
        return ResponseEntity.ok(verificationService.rejectVerification(verification_id, reason));
    }


    @GetMapping("/my-requests")
    public ResponseEntity<Iterable<VerificationResponse>> getMyRequests(){
        return ResponseEntity.ok(verificationService.getCurrentUserVerifications());
    }

    @GetMapping @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<VerificationListResponse> getAll(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,

            @Parameter(description = "Filter by status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false)VerificationRequestStatus status,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort order (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortOrder
    ){
        return ResponseEntity.ok(verificationService.getAll(page, limit, status, sortBy, sortOrder));
    }
}
