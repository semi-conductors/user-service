package com.rentmate.service.user.domain.mapper;

import com.rentmate.service.user.domain.dto.rating.CreateRatingRequest;
import com.rentmate.service.user.domain.dto.rating.RatingResponse;
import com.rentmate.service.user.domain.dto.rating.UserRatingDto;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.UserRating;

import java.time.LocalDateTime;

public class RatingMapper {
    public static UserRating toUserRating(CreateRatingRequest request, User rater, User rated) {
        UserRating userRating = new UserRating();
        userRating.setRating(request.getRating().byteValue());
        userRating.setFeedback(request.getFeedback());
        userRating.setRatedUser(rated);
        userRating.setRater(rater);
        userRating.setCreatedAt(LocalDateTime.now());
        userRating.setUpdatedAt(LocalDateTime.now());
        userRating.setRentalRequestId(request.getRentalRequestId());
        return userRating;
    }

    public static RatingResponse toRatingResponse(UserRating userRating, String raterName) {
        return new RatingResponse(
          userRating.getId(),
          userRating.getRater().getId(),
          userRating.getRatedUser().getId(),
          raterName,
          userRating.getRating().intValue(),
          userRating.getFeedback(),
          userRating.getCreatedAt()
        );
    }

    public static RatingResponse toRatingResponse(UserRatingDto dto, Long userId) {
        return new RatingResponse(
          dto.getId(),
          dto.getRater().getId(),
          userId,
          dto.getRater().getFirstName() + " " + dto.getRater().getLastName(),
          dto.getRating().intValue(),
          dto.getFeedback(),
          dto.getCreatedAt()
        );
    }
}
