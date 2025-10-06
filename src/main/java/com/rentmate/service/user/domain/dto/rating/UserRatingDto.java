package com.rentmate.service.user.domain.dto.rating;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRatingDto {
    private final Long id;
    private final Byte rating;
    private final String feedback;
    private final LocalDateTime createdAt;
    private final RaterDto rater;

    // Note: accept rater fields and build a RaterDtoImpl inside
    public UserRatingDto(Long id, Byte rating, String feedback, LocalDateTime createdAt,
                             Long raterId, String raterFirstName, String raterLastName) {
        this.id = id; this.rating = rating; this.feedback = feedback; this.createdAt = createdAt;
        this.rater = new RaterDto(raterId, raterFirstName, raterLastName);
    }
}