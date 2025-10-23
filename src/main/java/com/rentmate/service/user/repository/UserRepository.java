package com.rentmate.service.user.repository;

import com.rentmate.service.user.domain.dto.user.PublicUserProfileResponse;
import com.rentmate.service.user.domain.dto.user.UsernameDto;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);

    @Query("SELECT true FROM User u WHERE u.email = ?1 OR u.phoneNumber = ?2")
    Optional<Boolean> userExists(String email, String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.isDisabled = false")
    Optional<User> findNotDisabledByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.isDisabled = true, u.activityStatus =:status WHERE u.id = :id")
    void deactivateUser(@Param("id") Long id,@Param("status") AccountActivityStatus status);

    @Modifying
    @Query("UPDATE User u SET u.isIdentityVerified = true WHERE u.id = :id")
    void verifyUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.averageRating = (
            SELECT COALESCE(AVG(r.rating), 0)
            FROM UserRating r
            WHERE r.ratedUser.id = :userId
        ), u.totalRating = (
                SELECT COUNT(r)
                FROM UserRating r
                WHERE r.ratedUser.id = :userId
        )
        WHERE u.id = :userId
    """)
    void updateAverageRating(@Param("userId") Long userId);

    <T> Optional<T> findById(Long id, Class<T> type);

    <T> List<T> findByIdIn(Collection<Long> ids, Class<T> type);
}
