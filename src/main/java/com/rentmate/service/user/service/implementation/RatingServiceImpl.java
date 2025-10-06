package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.domain.dto.PagedResponse;
import com.rentmate.service.user.domain.dto.rating.*;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.UserRating;
import com.rentmate.service.user.domain.mapper.RatingMapper;
import com.rentmate.service.user.repository.RatingRepository;
import com.rentmate.service.user.repository.UserRepository;
import com.rentmate.service.user.service.RatingService;
import com.rentmate.service.user.service.UserService;
import com.rentmate.service.user.service.shared.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Override @Transactional
    public RatingResponse createRating(Long raterId, CreateRatingRequest request) {
        // TODO: check if ratalRequestId is valid from the rental service

        if(!userRepository.existsById(request.getRatedUserId()))
            throw new NotFoundException("User with id " + request.getRatedUserId() + " not found");

        if(ratingRepository.doesExist(request.getRentalRequestId()).orElse(false))
            throw new BadRequestException("Rating already exists for the specified rental ID");

        User rater = new User(); rater.setId(raterId);
        User ratedUser = new User(); ratedUser.setId(request.getRatedUserId());

        UserRating rating = RatingMapper.toUserRating(request, rater, ratedUser);
        ratingRepository.save(rating);
        userRepository.updateAverageRating(ratedUser.getId());

        return RatingMapper.toRatingResponse(rating, UserService.getAuthenticatedUser().getUsername());
    }

    @Override
    public PagedResponse<RatingResponse> getRatingsByUserId(Long userId, Integer page, Integer limit) {
        Pageable pageable = PageRequest
                .of(page-1, limit, Sort.by("createdAt").descending());

        var result = ratingRepository.findAllByRatedUserId(userId, pageable);

        return new PagedResponse<>(
                page,
                result.getTotalPages(),
                result.getTotalElements(),
                limit,
                result.hasNext(),
                result.hasPrevious(),
                result.getContent()
                        .stream()
                        .map((userRatingDto -> RatingMapper.toRatingResponse(userRatingDto, userId)))
                        .toList()
        );
    }

    @Override @Transactional
    public RatingResponse updateRating(UpdateRatingRequest request) {
        UserRating rating = ratingRepository.findById(request.getRatingId()).orElseThrow(() -> new NotFoundException("Rating not found"));

        if(!rating.getRater().getId().equals(UserService.getAuthenticatedUser().getId()))
            throw new ForbiddenActionException("You are not the rater of this rating");

        rating.setRating(request.getRating().byteValue());
        rating.setFeedback(request.getFeedback());
        rating.setUpdatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        userRepository.updateAverageRating(rating.getRatedUser().getId());

        return RatingMapper.toRatingResponse(rating, UserService.getAuthenticatedUser().getUsername());
    }

    @Override @Transactional
    public void deleteRating(Long ratingId) {
        UserRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        if(!rating.getRater().getId().equals(UserService.getAuthenticatedUser().getId()))
            throw new ForbiddenActionException("You are not the rater of this rating");

        ratingRepository.delete(rating);
        userRepository.updateAverageRating(rating.getRatedUser().getId());
    }
}
