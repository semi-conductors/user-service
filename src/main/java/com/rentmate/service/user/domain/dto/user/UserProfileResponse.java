package com.rentmate.service.user.domain.dto.user;

import java.time.LocalDate;

public record UserProfileResponse(Long id, String email, String username,
                                  boolean isVerified, String role, String phoneNumber,
                                  String accountActivity, Double rating,
                                  Integer totalRatings, LocalDate memberSince) {
}