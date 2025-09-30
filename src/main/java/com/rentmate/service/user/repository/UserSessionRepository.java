package com.rentmate.service.user.repository;

import com.rentmate.service.user.domain.entity.UserSession;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository extends CrudRepository<UserSession, Long> {
    @Modifying
    @Query("UPDATE UserSession session SET session.isActive = false WHERE session.isActive=true AND session.token = :token")
    int deactivateSession(@Param("token") String token);

    @Query("""
       SELECT session FROM UserSession session JOIN FETCH session.user
       WHERE session.token = :token AND session.isActive = true AND session.expiresAt > :now
       """)
    Optional<UserSession> findActiveSession(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession session SET session.isActive = false WHERE session.isActive=true AND session.user.id = :userId")
    int deactivateSessionsForUser(Long userId);
}
