package com.rentmate.service.user.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class PasswordResetRequestedEvent {
    private String email;
    private String resetToken;
    private LocalDateTime expiresAt;
}