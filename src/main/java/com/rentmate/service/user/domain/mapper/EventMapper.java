package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.event.PasswordResetRequestedEvent;
import com.rentmate.service.user.domain.dto.event.UserRegisteredEvent;
import com.rentmate.service.user.domain.entity.PasswordResetToken;
import com.rentmate.service.user.domain.entity.User;

public class EventMapper {
    public static UserRegisteredEvent toUserRegisteredEvent(User user) {
        return new UserRegisteredEvent(
                user.getId(),
                user.getFirstName()+ " " +user.getLastName(),
                user.getEmail(),
                user.getRole().toString(), user.getCreatedAt()
        );
    }

//    public static PasswordResetRequestedEvent toPasswordResetRequestedEvent(String email, String rawToken) {
//        return new PasswordResetRequestedEvent(
//                token.getUser().getEmail(),
//                rawToken,
//                token.getExpiresAt()
//        );
//    }
}
