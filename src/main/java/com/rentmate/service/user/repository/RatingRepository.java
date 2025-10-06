package com.rentmate.service.user.repository;

import com.rentmate.service.user.domain.dto.rating.RatingResponse;
import com.rentmate.service.user.domain.dto.rating.UserRatingDto;
import com.rentmate.service.user.domain.entity.UserRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<UserRating , Long> {

    @Query("SELECT true FROM UserRating ur WHERE ur.rentalRequestId = ?1")
    Optional<Boolean> doesExist(Long rentalRequestId);

    @Query(value = """
        SELECT new com.rentmate.service.user.domain.dto.rating.UserRatingDto(
            ur.id, ur.rating, ur.feedback, ur.createdAt,
            r.id, r.firstName, r.lastName
        )
        FROM UserRating ur
        JOIN ur.rater r
        WHERE ur.ratedUser.id = :userId
        """,
            countQuery = "SELECT count(ur) FROM UserRating ur WHERE ur.ratedUser.id = :userId")
    Page<UserRatingDto> findAllByRatedUserId(@Param("userId") Long userId, Pageable pageable);

}