package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.event.PasswordResetRequestedEvent;
import com.rentmate.service.user.domain.dto.event.*;
import com.rentmate.service.user.domain.entity.PasswordResetToken;
import com.rentmate.service.user.domain.entity.User;

import java.time.LocalDateTime;

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

    public static ProfileDisabledEvent toProfileDisabledEvent(User user, String reason, String disabledBy) {
        return new ProfileDisabledEvent(
                user.getId(),
                user.getEmail(),
                user.getFirstName()+ " " +user.getLastName(),
                disabledBy,
                reason,
                LocalDateTime.now()
        );
    }
}
