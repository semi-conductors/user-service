package com.rentmate.service.user.domain.dto.auth;

import jakarta.validation.constraints.Email;

public record PasswordResetTokenRequest(@Email String email) {
}
