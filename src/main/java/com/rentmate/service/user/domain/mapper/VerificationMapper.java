package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.verification.CreateVerificationRequest;
import com.rentmate.service.user.domain.dto.verification.VerificationDetailsResponse;
import com.rentmate.service.user.domain.dto.verification.VerificationResponse;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.VerificationRequest;

import java.time.LocalDateTime;

public class VerificationMapper {
    public static VerificationResponse toVerificationResponse(VerificationRequest verification) {
        return new VerificationResponse(
                verification.getId(),
                verification.getUser().getId(),
                verification.getIdFrontImageUrl(),
                verification.getIdBackImageUrl(),
                verification.getIdNumber(),
                verification.getStatus(),
                verification.getCreatedAt()
        );
    }

    public static VerificationRequest toVerificationRequest(CreateVerificationRequest request, User user) {
        var result = new VerificationRequest();
        result.setCreatedAt(LocalDateTime.now());
        result.setIdFrontImageUrl(request.getIdFrontImageUrl());
        result.setIdBackImageUrl(request.getIdBackImageUrl());
        result.setIdNumber(request.getIdNumber());
        result.setUser(user);
        return result;
    }

    public static VerificationDetailsResponse toVerificationDetailsResponse(VerificationRequest verification) {
        return new VerificationDetailsResponse(
                verification.getId(),
                verification.getIdFrontImageUrl(),
                verification.getIdBackImageUrl(),
                verification.getIdNumber(),
                verification.getStatus(),
                verification.getCreatedAt(),
                UserMapper.toUserProfileResponse(verification.getUser()),
                verification.getReviewedBy() == null ? null : UserMapper.toUserProfileResponse(verification.getReviewedBy())
        );
    }
}
