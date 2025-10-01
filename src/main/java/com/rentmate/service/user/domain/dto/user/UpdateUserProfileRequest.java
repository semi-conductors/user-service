package com.rentmate.service.user.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record UpdateUserProfileRequest(
        @NotBlank(message = "First name is required")
        @Length(max = 100,min = 3, message = "First name must be between 3 and 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Length(max = 100, min=3, message = "Last name must be between 3 and 100 characters")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Size(min = 11, max = 11, message = "Phone number must be 11 characters")
        @Pattern(regexp = "^[0-9]*$", message = "Phone number must be numeric")
        String phoneNumber) {
}
