package com.rentmate.service.user.domain.dto.event;

import lombok.Data;
import java.time.LocalDateTime;

public record UserRegisteredEvent(Long userId, String username, String email, String role, LocalDateTime registeredAt) {
}