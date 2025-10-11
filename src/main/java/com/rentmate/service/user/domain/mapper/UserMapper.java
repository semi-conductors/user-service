package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.user.*;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    public static User toUser(CreateProfileRequest request, PasswordEncoder encoder) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setActivityStatus(AccountActivityStatus.ACTIVE);
        user.setRole(request.getRole());
        return user;
    }

}
