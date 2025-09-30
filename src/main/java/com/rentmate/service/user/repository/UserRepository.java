package com.rentmate.service.user.repository;

import com.rentmate.service.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);

    @Query("SELECT true FROM User u WHERE u.email = ?1 OR u.phoneNumber = ?2")
    Optional<Boolean> userExists(String email, String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.isDisabled = false")
    Optional<User> findNotDisabledByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.isDisabled = true WHERE u.id = :id")
    void disableUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
