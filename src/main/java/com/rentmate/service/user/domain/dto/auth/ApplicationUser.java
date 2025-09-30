package com.rentmate.service.user.domain.dto.auth;

public record ApplicationUser(Long id, String email, String username, boolean isVerified, String role) {
}
