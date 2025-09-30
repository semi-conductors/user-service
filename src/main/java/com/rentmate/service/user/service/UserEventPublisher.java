package com.rentmate.service.user.service;

import com.rentmate.service.user.domain.dto.event.PasswordResetRequestedEvent;
import com.rentmate.service.user.domain.dto.event.UserRegisteredEvent;

public interface UserEventPublisher {
    void publishUserRegistered(UserRegisteredEvent event);
    void publishPasswordResetRequestedEvent(PasswordResetRequestedEvent event);
}
