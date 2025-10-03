package com.rentmate.service.user.service;

import com.rentmate.service.user.domain.dto.event.*;


public interface UserEventPublisher {
    void publishUserRegistered(UserRegisteredEvent event);
    void publishPasswordResetRequestedEvent(PasswordResetRequestedEvent event);
    void publishProfileDisabledEvent(ProfileDisabledEvent event);
    void publishIdentityVerificationApprovedEvent(IdentityVerificationApprovedEvent event);
    void publishIdentityVerificationRejectedEvent(IdentityVerificationRejectedEvent event);
}
