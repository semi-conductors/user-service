package com.rentmate.service.user.domain.dto.user;

import com.rentmate.service.user.domain.enumuration.UserRole;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PublicUserProfileResponse {
    private final Long id;
    private final String userName;
    private final Boolean isVerified;
    private final Double rating;
    private final Integer totalRatings;
    private final String role;

    public PublicUserProfileResponse(Long id, String firstName, String lastName,
            boolean isIdentityVerified, BigDecimal averageRating, Integer totalRating, UserRole role) {
        this.id = id;
        this.userName = firstName + " " + lastName;
        this.isVerified = isIdentityVerified;
        this.rating = averageRating.doubleValue();
        this.totalRatings = totalRating;
        this.role = role.name();
    }
}
