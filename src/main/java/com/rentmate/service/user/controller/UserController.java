package com.rentmate.service.user.controller;

import com.rentmate.service.user.domain.dto.user.*;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
import com.rentmate.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateUserProfileRequest userProfileRequest) {
        return ResponseEntity.ok(userService.updateProfile(userProfileRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicUserProfileResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicUserProfile(id));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deactivateOwnProfile() {
        userService.disableOwnProfile();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserProfileResponse> updateProfileStatus(@PathVariable Long id, @RequestBody UpdateProfileStatusRequest request) {
        return ResponseEntity.ok(userService.updateProfileStatus(id, request));
    }

    @PatchMapping("/{id}/role") @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserProfileResponse> updateProfileRole(@PathVariable Long id, @RequestBody UpdateProfileRoleRequest request) {
        return ResponseEntity.ok(userService.updateProfileRole(id, request));
    }
    @PostMapping @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserProfileResponse> createProfile(@Valid @RequestBody CreateProfileRequest userProfileRequest) {
        return ResponseEntity.ok(userService.createProfile(userProfileRequest));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "List all users",
            description = "Retrieve paginated list of all users with filtering and search capabilities. Admin/Manager only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserListResponse> getAllUsers(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,

            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,

            @Parameter(description = "Filter by role (USER, DELIVERY_GUY, ADMIN, MANAGER)")
            @RequestParam(required = false) UserRole role,

            @Parameter(description = "Filter by account status")
            @RequestParam(required = false) AccountActivityStatus status,

            @Parameter(description = "Filter by verification status")
            @RequestParam(required = false) Boolean isVerified,

            @Parameter(description = "Search by name, email, or phone (min 2 characters)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort order (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {

        UserListResponse response = userService.getAllUsers(
                page, limit, role, status, isVerified, search, sortBy, sortOrder
        );
        return ResponseEntity.ok(response);
    }
}
