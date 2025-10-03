package com.rentmate.service.user.domain.dto.verification;

import com.rentmate.service.user.domain.dto.user.PublicUserProfileResponse;
import com.rentmate.service.user.domain.dto.user.UserProfileResponse;
import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;

import java.time.LocalDateTime;

public record VerificationDetailsResponse(Long id, String idFrontImageUrl, String idBackImageUrl,
                                          String idNumber, VerificationRequestStatus status, LocalDateTime submittedAt,
                                          UserProfileResponse submittedBy, UserProfileResponse verifiedBy) {
}
