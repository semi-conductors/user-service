package com.rentmate.service.user.domain.dto.event;

import java.time.LocalDateTime;

public record ProfileDisabledEvent(Long userId, String email, String username,
                                   String disabledBy, String reason, LocalDateTime disabledAt) {
}
