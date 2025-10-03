package com.rentmate.service.user.domain.dto.event;

import java.time.LocalDateTime;

public record IdentityVerificationRejectedEvent(Long userId, Long verificationRequestId,
                                                String email, LocalDateTime createdAt, LocalDateTime rejectedAt,
                                                String rejectionReason) {
}
