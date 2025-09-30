package com.rentmate.service.user.repository;

import com.rentmate.service.user.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {
    @Query("select p from PasswordResetToken p where p.token = :token and p.expiresAt > :now and p.usedAt is null")
    Optional<PasswordResetToken> findUsableToken(String token, LocalDateTime now);
}
