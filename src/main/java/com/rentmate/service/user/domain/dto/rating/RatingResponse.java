package com.rentmate.service.user.domain.dto.rating;

import java.time.LocalDateTime;

public record RatingResponse(Long id, Long raterUserId,Long ratedUserId, String raterUserName, Integer rating, String feedback, LocalDateTime createdAt) {
}
