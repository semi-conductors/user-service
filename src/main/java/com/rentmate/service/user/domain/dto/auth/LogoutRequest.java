package com.rentmate.service.user.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

public record LogoutRequest(@NotBlank String refreshToken) {
}
