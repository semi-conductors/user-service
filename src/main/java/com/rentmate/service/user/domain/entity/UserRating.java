package com.rentmate.service.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_ratings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_rental_rating", columnNames = {"rental_request_id", "rater_id"})
        })
@Data
public class UserRating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Byte rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rater_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rating_rater"))
    private User rater;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rated_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rating_rated"))
    private User ratedUser;

    @Column(name = "rental_request_id", nullable = false)
    private Long rentalRequestId;
}
