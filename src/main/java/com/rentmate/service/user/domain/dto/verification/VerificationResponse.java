package com.rentmate.service.user.domain.dto.verification;

import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;

import java.time.LocalDateTime;

public record VerificationResponse(Long id, Long userId, String idFrontImageUrl, String idBackImageUrl,
                                   String idNumber, VerificationRequestStatus status, LocalDateTime submittedAt) {
}
