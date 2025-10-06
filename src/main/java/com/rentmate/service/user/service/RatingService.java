package com.rentmate.service.user.service;

import com.rentmate.service.user.domain.dto.PagedResponse;
import com.rentmate.service.user.domain.dto.rating.CreateRatingRequest;
import com.rentmate.service.user.domain.dto.rating.RatingResponse;
import com.rentmate.service.user.domain.dto.rating.UpdateRatingRequest;

public interface RatingService {
    RatingResponse createRating(Long raterId, CreateRatingRequest request);
    PagedResponse<RatingResponse> getRatingsByUserId(Long userId, Integer page, Integer limit);
    RatingResponse updateRating(UpdateRatingRequest request);
    void deleteRating(Long ratingId);
}