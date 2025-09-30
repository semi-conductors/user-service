package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.auth.ApplicationUser;
import com.rentmate.service.user.domain.dto.auth.RegisterRequest;
import com.rentmate.service.user.domain.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthMapper {
    public static User toUser(RegisterRequest request, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        return user;
    }

    public static ApplicationUser toApplicationUser(User user) {
        return new ApplicationUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                user.isIdentityVerified(),
                user.getRole().toString()
        );
    }
}
