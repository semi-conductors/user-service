package com.rentmate.service.user.domain.dto.user;

import com.rentmate.service.user.domain.dto.auth.RegisterRequest;
import com.rentmate.service.user.domain.enumuration.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProfileRequest extends RegisterRequest {
    @NotNull
    private UserRole role;
}
