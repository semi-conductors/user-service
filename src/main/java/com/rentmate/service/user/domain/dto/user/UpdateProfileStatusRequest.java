package com.rentmate.service.user.domain.dto.user;

import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileStatusRequest (@NotNull AccountActivityStatus status, @NotBlank String reason) {
}
