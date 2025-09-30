package com.rentmate.service.user.domain.dto.auth;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.NumberFormat;

@Data
public class RegisterRequest {
    @NotNull(message = "First name is required")
    @Length(max = 100, message = "First name must be less than 100 characters")
    private String firstName;
    @NotNull(message = "Last name is required")
    @Length(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Password is required")
    @Length(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotNull(message = "Phone number is required")
    @Size(min = 11, max = 11, message = "Phone number must be 11 characters")
    @Pattern(regexp = "^[0-9]*$", message = "Phone number must be numeric")
    private String phoneNumber;
}
