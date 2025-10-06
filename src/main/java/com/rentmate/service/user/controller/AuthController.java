package com.rentmate.service.user.controller;

import com.rentmate.service.user.domain.dto.auth.PasswordResetTokenRequest;
import com.rentmate.service.user.domain.dto.auth.*;
import com.rentmate.service.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints for user account management")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user account with USER role (default). Returns authentication tokens upon successful registration. " +
                    "Email and phone number must be unique. Password is securely hashed before storage."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully registered and authenticated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Email or phone number already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<LoginResponse> register(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and generate tokens",
            description = "Validates user credentials and returns JWT access token (short-lived) and refresh token (long-lived). " +
                    "Account must not be disabled. Use access token for authenticated requests and refresh token to obtain new access tokens."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials or account disabled",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401," +
                                            "\"detail\":\"Invalid email or password\",\"instance\":\"/users/auth/login\"}"
                            )
                    )
            )
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "User login credentials", required = true)
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout and invalidate refresh token",
            description = "Terminates the current user session by invalidating the refresh token. " +
                    "The access token will remain valid until expiration but the refresh token cannot be used to generate new tokens. " +
                    "All active sessions for the device will be terminated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully logged out",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Refresh token not found or already invalidated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token to invalidate", required = true)
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new JWT access token using a valid refresh token. " +
                    "The refresh token must be active and not expired. A new refresh token is also issued, " +
                    "and the old refresh token is invalidated to prevent reuse."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "New access token generated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RefreshResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid refresh token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401," +
                                            "\"detail\":\"Refresh token expired or invalid\",\"instance\":\"/users/auth/refresh\"}"
                            )
                    )
            )
    })
    public ResponseEntity<RefreshResponse> refresh(
            @Parameter(description = "Valid refresh token", required = true)
            @Valid @RequestBody LogoutRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/password-reset/token")
    @Operation(
            summary = "Request password reset token",
            description = "Initiates the password reset process by generating a unique, time-limited token. " +
                    "If the email exists in the system, a reset token is sent via email (handled by notification service). " +
                    "Token expires after 60 minutes by default. For security, the response is always 200 OK regardless of email existence."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset request processed (check email if account exists)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<Void> sendResetToken(
            @Parameter(description = "Email address of the account to reset", required = true)
            @Valid @RequestBody PasswordResetTokenRequest request
    ) {
        authService.sendResetToken(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/confirm")
    @Operation(
            summary = "Confirm password reset with token",
            description = "Completes the password reset process using the token received via email. " +
                    "The token must be valid, unused, and not expired. Upon successful reset, " +
                    "the token is marked as used and cannot be reused. Password must meet security requirements (6-100 characters)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password successfully reset",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Invalid token, or expired token.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400," +
                                            "\"detail\":\"Reset token is invalid or expired\",\"instance\":\"/users/auth/password-reset/confirm\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "Password reset confirmation with token and new password", required = true)
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}