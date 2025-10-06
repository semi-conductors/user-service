package com.rentmate.service.user.controller;

import com.rentmate.service.user.domain.dto.PagedResponse;
import com.rentmate.service.user.domain.dto.rating.CreateRatingRequest;
import com.rentmate.service.user.domain.dto.rating.RatingResponse;
import com.rentmate.service.user.domain.dto.rating.UpdateRatingRequest;
import com.rentmate.service.user.service.RatingService;
import com.rentmate.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Ratings & Feedback", description = "Endpoints for managing user ratings and feedback system. " +
        "Users can rate each other after completed transactions to build trust profiles. " +
        "Ratings automatically update denormalized average_rating and total_ratings on user records.")
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/ratings")
    @Operation(
            summary = "Submit a rating for another user",
            description = "Creates a new rating (1-5 stars) with optional feedback for another user after a completed rental transaction. " +
                    "Constraints and validations: " +
                    "- Rating can only be submitted once per rental request (enforced by unique constraint on rental_request_id + rater_id) " +
                    "- Rated user must exist in the system " +
                    "- Rental request must be COMPLETED (validation to be implemented via cross-service check) " +
                    "- Rating value must be between 1-5 (inclusive) " +
                    "- Automatically updates the rated user's average_rating and total_ratings fields " +
                    "Used to build trust and reputation in the platform.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Rating submitted successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RatingResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Rated user not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<RatingResponse> createRating(
            @Parameter(description = "Rating details including rated user ID, rental request ID, rating value (1-5), and optional feedback", required = true)
            @Valid @RequestBody CreateRatingRequest request
    ) {
        RatingResponse response = ratingService.createRating(UserService.getAuthenticatedUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/users/ratings/" + response.id()))
                .body(response);
    }

    @GetMapping("/{userId}/ratings")
    @Operation(
            summary = "Get all ratings received by a user",
            description = "Retrieves a paginated list of all ratings and feedback received by a specific user. " +
                    "Results are sorted by creation date (most recent first). " +
                    "Includes rating score, feedback text, and rater information. " +
                    "Useful for viewing a user's trust profile and reputation before engaging in transactions. " +
                    "Available to all authenticated users for transparency.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Ratings retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PagedResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "User not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<PagedResponse<RatingResponse>> getUserRatings(
            @Parameter(description = "User ID whose ratings to retrieve", required = true, example = "123")
            @PathVariable Long userId,
            @Parameter(description = "Page number (1-based indexing)")
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "Number of items per page (max 100)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(ratingService.getRatingsByUserId(userId, page, limit));
    }

    @PutMapping("/ratings")
    @Operation(
            summary = "Update an existing rating",
            description = "Allows the original rater to update their previously submitted rating and feedback. " +
                    "Only the user who created the rating can update it (ownership verified by comparing rater_id with authenticated user). " +
                    "Can modify: " +
                    "- Rating value (1-5 stars) " +
                    "- Feedback text " +
                    "Automatically recalculates the rated user's average_rating and total_ratings. " +
                    "Updated timestamp is recorded for audit purposes.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Rating updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RatingResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Not authorized to update this rating - only the original rater can update",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Rating not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<RatingResponse> updateRating(
            @Parameter(description = "Updated rating information including rating ID, new rating value (1-5), and feedback", required = true)
            @Valid @RequestBody UpdateRatingRequest request
    ) {
        return ResponseEntity.ok(ratingService.updateRating(request));
    }

    @DeleteMapping("/ratings/{ratingId}")
    @Operation(
            summary = "Delete a rating",
            description = "Allows the original rater to delete their previously submitted rating. " +
                    "Only the user who created the rating can delete it (ownership verified by comparing rater_id with authenticated user). " +
                    "This is a hard delete - the rating is permanently removed from the database. " +
                    "Automatically recalculates the rated user's average_rating and total_ratings after deletion. " +
                    "Use cases: " +
                    "- User wants to retract their rating " +
                    "- Rating was submitted in error " +
                    "Note: Consider implementing soft delete (is_visible flag) instead for audit trail preservation.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204", description = "Rating deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Not authorized to delete this rating - only the original rater can delete",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Rating not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<Void> deleteRating(
            @Parameter(description = "Rating ID to delete", required = true, example = "123")
            @PathVariable Long ratingId
    ) {
        ratingService.deleteRating(ratingId);
        return ResponseEntity.noContent().build();
    }
}