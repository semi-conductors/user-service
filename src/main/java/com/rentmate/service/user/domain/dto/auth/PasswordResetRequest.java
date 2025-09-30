package com.rentmate.service.user.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordResetRequest {
    @NotBlank(message = "Token is required")
    private String token;

    @NotNull(message = "Password is required")
    @Length(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String newPassword;
}
