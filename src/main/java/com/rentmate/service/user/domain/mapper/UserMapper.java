package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.user.*;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;

public class UserMapper {
    public static UserProfileResponse toUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName()+ " " + user.getLastName(),
                user.isIdentityVerified(),
                user.getRole().toString(),
                user.getPhoneNumber(),
                user.getActivityStatus().toString(),
                user.getAverageRating().doubleValue(),
                user.getTotalRating(),
                user.getCreatedAt().toLocalDate()
        );
    }

    public static User toUser(CreateProfileRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setActivityStatus(AccountActivityStatus.ACTIVE);
        user.setRole(request.getRole());
        return user;
    }
}
