package com.rentmate.service.user.domain.dto.event;

import java.time.LocalDateTime;

public record IdentityVerificationApprovedEvent(Long userId,Long verificationRequestId,
                                    String email, LocalDateTime createdAt, LocalDateTime approvedAt) {
}
