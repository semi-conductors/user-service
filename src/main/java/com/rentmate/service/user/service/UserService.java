package com.rentmate.service.user.service;

import com.rentmate.service.user.domain.dto.user.*;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
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

    static Long getAuthenticatedUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new RuntimeException("No authentication found");
        }
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }
}
