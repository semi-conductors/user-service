package com.rentmate.service.user.domain.dto.user;

import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.enumuration.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserProfileResponse(Long id, String email, String username,
                                  boolean isVerified, String role, String phoneNumber,
                                  String accountActivity, Double rating,
                                  Integer totalRatings, LocalDate memberSince) {
    public UserProfileResponse(Long id, String firstName, String lastName, String email,
                               String phoneNumber, UserRole role, boolean isIdentityVerified,
                               String accountActivity, Double averageRating,
                               Integer totalRating, LocalDateTime createdAt) {
        this(id, email, firstName+ " " + lastName, isIdentityVerified, role.name(), phoneNumber, accountActivity, averageRating, totalRating, createdAt.toLocalDate());
    }
}