package com.rentmate.service.user.controller;

import com.rentmate.service.user.domain.dto.user.*;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
import com.rentmate.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "Endpoints for managing user profiles, roles, and account status. " +
        "Includes both self-service operations and administrative functions.")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Get authenticated user's profile",
            description = "Retrieves the complete profile information of the currently authenticated user, " +
                    "including personal details, verification status, ratings, and account activity status. " +
                    "Requires valid JWT access token in Authorization header.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"type\":\"about:blank\",\"title\":\"Not Found\",\"status\":404," +
                                            "\"detail\":\"User profile not found\",\"instance\":\"/users/profile\"}"
                            )
                    )
            )
    })
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update authenticated user's profile",
            description = "Updates the personal information of the currently authenticated user. " +
                    "Allows modification of first name, last name, and phone number. " +
                    "Email and role cannot be changed through this endpoint. " +
                    "Phone number must be unique across all users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(description = "Updated profile information (first name, last name, phone number)", required = true)
            @Valid @RequestBody UpdateUserProfileRequest userProfileRequest
    ) {
        return ResponseEntity.ok(userService.updateProfile(userProfileRequest));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get public user profile by ID",
            description = "Retrieves public profile information for any user by their ID. " +
                    "Returns only non-sensitive information including username, verification status, " +
                    "ratings, and role. Useful for viewing profiles of other users before transactions. " +
                    "Available to all authenticated users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Public profile retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicUserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"type\":\"about:blank\",\"title\":\"Not Found\",\"status\":404," +
                                            "\"detail\":\"User profile not found\",\"instance\":\"/users/123\"}"
                            )
                    )
            )
    })
    public ResponseEntity<PublicUserProfileResponse> getUser(
            @Parameter(description = "User ID to retrieve", required = true, example = "123")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.getPublicUserProfile(id));
    }

    @DeleteMapping("/profile")
    @Operation(
            summary = "Deactivate own account (soft delete)",
            description = "Allows authenticated users to deactivate their own account. " +
                    "This is a soft delete operation - account is marked as disabled (is_disabled = true) " +
                    "and status set to INACTIVE. All active sessions are terminated. " +
                    "User data is preserved in the database for audit purposes. " +
                    "Note: This operation checks for active rentals (to be implemented) before allowing deactivation.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Account successfully deactivated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot deactivate account with active rentals (future implementation)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<Void> deactivateOwnProfile() {
        userService.disableOwnProfile();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Update user account status (Admin/Manager only)",
            description = "Allows administrators to change the account activity status of any user. " +
                    "Possible statuses: ACTIVE, INACTIVE, PENDING_REPORT_REVIEW, SUSPENDED_BY_ADMIN. " +
                    "When status is set to SUSPENDED_BY_ADMIN, the account is disabled and all active sessions are terminated. " +
                    "A ProfileDisabledEvent is published for suspended accounts. Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account status updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value or missing reason",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403," +
                                            "\"detail\":\"Access denied\",\"instance\":\"/users/123/status\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<UserProfileResponse> updateProfileStatus(
            @Parameter(description = "User ID whose status to update", required = true, example = "123")
            @PathVariable Long id,
            @Parameter(description = "New account status and reason for the change", required = true)
            @Valid @RequestBody UpdateProfileStatusRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfileStatus(id, request));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Update user role (Admin/Manager only)",
            description = "Allows administrators to change the role of any user. " +
                    "Available roles: USER (1), DELIVERY_GUY (2), ADMIN (3), MANAGER (4). " +
                    "This operation should enforce role hierarchy restrictions (e.g., managers cannot promote users to manager role, " +
                    "users cannot demote themselves). Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User role updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid role value or role hierarchy violation",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<UserProfileResponse> updateProfileRole(
            @Parameter(description = "User ID whose role to update", required = true, example = "123")
            @PathVariable Long id,
            @Parameter(description = "New role for the user", required = true)
            @Valid @RequestBody UpdateProfileRoleRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfileRole(id, request));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Create new user account (Admin/Manager only)",
            description = "Allows administrators to create new user accounts with any role (USER, DELIVERY_GUY, ADMIN). " +
                    "Useful for onboarding platform staff (delivery personnel, admins) who don't self-register. " +
                    "Password is securely hashed before storage. Email and phone number must be unique. " +
                    "A UserRegisteredEvent is published upon successful creation. Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User account created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation failed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email or phone number already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"type\":\"about:blank\",\"title\":\"Conflict\",\"status\":409," +
                                            "\"detail\":\"User with this email or phone number already exists\",\"instance\":\"/users\"}"
                            )
                    )
            )
    })
    public ResponseEntity<UserProfileResponse> createProfile(
            @Parameter(description = "User profile information including role", required = true)
            @Valid @RequestBody CreateProfileRequest userProfileRequest
    ) {
        return ResponseEntity.ok(userService.createProfile(userProfileRequest));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "List all users with filtering and pagination (Admin/Manager only)",
            description = "Retrieves a paginated list of all users in the system with advanced filtering capabilities. " +
                    "Supports filtering by role, account status, verification status, and keyword search (name, email, phone). " +
                    "Results can be sorted by various fields. Minimum search query length is 2 characters. " +
                    "Useful for user management and moderation. Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserListResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or filter parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions - requires ADMIN or MANAGER role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<UserListResponse> getAllUsers(
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,

            @Parameter(description = "Filter by user role", example = "USER",
                    schema = @Schema(allowableValues = {"USER", "DELIVERY_GUY", "ADMIN", "MANAGER"}))
            @RequestParam(required = false) UserRole role,

            @Parameter(description = "Filter by account activity status",
                    schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE", "PENDING_REPORT_REVIEW", "SUSPENDED_BY_ADMIN"}))
            @RequestParam(required = false) AccountActivityStatus status,

            @Parameter(description = "Filter by identity verification status")
            @RequestParam(required = false) Boolean isVerified,

            @Parameter(description = "Search keyword for name, email, or phone number (minimum 2 characters)")
            @RequestParam(required = false) String search,

            @Parameter(
                    description = "Field to sort by",
                    schema = @Schema(allowableValues = {"createdAt", "updatedAt", "averageRating", "firstName"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(
                    description = "Sort direction",
                    schema = @Schema(allowableValues = {"asc", "desc"})
            )
            @RequestParam(defaultValue = "desc") String sortOrder) {
        UserListResponse response = userService.getAllUsers(
                page, limit, role, status, isVerified, search, sortBy, sortOrder
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    @Operation(
            summary = "Get user profile by ID (Admin/Manager only)",
            description = "Retrieves a single user profile by ID. Requires ADMIN or MANAGER role.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserProfileResponse> getById(@PathVariable Long id){
        var currUser = UserService.getAuthenticatedUser();
        if(currUser.getRole().equalsIgnoreCase("user") && !Objects.equals(id, currUser.getId()))
            throw new AccessDeniedException("You aren't authorized to access this resource");

        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/{id}/email")
    public ResponseEntity<?> getEmail(@PathVariable Long id){
        return ResponseEntity.ok(Map.of("email",userService.getUserProfile(id).email()));
    }
}