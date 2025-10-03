package com.rentmate.service.user.domain.dto.verification;

import java.time.LocalDateTime;

public record CanSubmitResponse(Boolean canSubmit, String message, LocalDateTime canSubmitAt) {
}
