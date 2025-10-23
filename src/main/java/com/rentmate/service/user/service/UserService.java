package com.rentmate.service.user.service;

import com.rentmate.service.user.domain.dto.user.*;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
import com.rentmate.service.user.service.shared.exception.NotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

public interface UserService {
    UserProfileResponse getUserProfile();

    UserProfileResponse updateProfile(UpdateUserProfileRequest request);

    PublicUserProfileResponse getPublicUserProfile(Long userId);

    void disableOwnProfile();

    UserProfileResponse updateProfileStatus(Long userId, UpdateProfileStatusRequest request);

    UserProfileResponse createProfile(CreateProfileRequest request);

    UserProfileResponse updateProfileRole(Long userId, UpdateProfileRoleRequest request);

    UserListResponse getAllUsers(Integer page, Integer limit, UserRole role, AccountActivityStatus status,
            Boolean isVerified, String search, String sortBy, String sortOrder);

    UserProfileResponse getUserProfile(Long userId);

    static Long getAuthenticatedUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new NotFoundException("No authentication found");
        }

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }

    static UserPrincipal getAuthenticatedUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new NotFoundException("No authentication found");
        }

        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
