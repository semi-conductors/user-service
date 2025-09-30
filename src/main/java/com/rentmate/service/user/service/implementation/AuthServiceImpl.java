package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.domain.dto.auth.*;
import com.rentmate.service.user.domain.dto.event.PasswordResetRequestedEvent;
import com.rentmate.service.user.domain.entity.PasswordResetToken;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.UserSession;
import com.rentmate.service.user.domain.mapper.*;
import com.rentmate.service.user.repository.PasswordResetTokenRepository;
import com.rentmate.service.user.repository.UserRepository;
import com.rentmate.service.user.repository.UserSessionRepository;
import com.rentmate.service.user.shared.util.JwtUtils;
import com.rentmate.service.user.service.AuthService;
import com.rentmate.service.user.service.UserEventPublisher;
import com.rentmate.service.user.shared.exception.NotFoundException;
import com.rentmate.service.user.shared.exception.RegistrationException;
import com.rentmate.service.user.shared.exception.SessionNotFoundException;
import com.rentmate.service.user.shared.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service @RequiredArgsConstructor @Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserEventPublisher eventPublisher;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Value("${user.session.expiration.time.days:30}")
    private long expirationTime;
    @Value("${user.password-reset-token-expiration-minutes}")
    private long minutesToExpire;


    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        User user = AuthMapper.toUser(request, passwordEncoder);

        if(userRepository.userExists(user.getEmail(), user.getPhoneNumber()).orElse(false))
            throw new RegistrationException("user already exist, chack your email and phone number.");

        userRepository.save(user);
        var response = login(user);

        eventPublisher.publishUserRegistered(EventMapper.toUserRegisteredEvent(user));
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findNotDisabledByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Invalid email or password");

        return login(user);
    }

    @Override @Transactional
    public void logout(String refreshToken) {
        int rows = userSessionRepository.deactivateSession(refreshToken);
        if(rows == 0){
            throw new SessionNotFoundException("no active session found with the given token");
        }
    }

    @Override
    public RefreshResponse refresh(String refreshToken) {
        UserSession userSession = userSessionRepository.findActiveSession(TokenUtils.hashToken(refreshToken), LocalDateTime.now())
                .orElseThrow(() -> new SessionNotFoundException("no active session found with the given token"));

        String token = jwtUtils.generateJwtToken(AuthMapper.toApplicationUser(userSession.getUser()));
        return new RefreshResponse(token);
    }

    @Override
    public void sendResetToken(String email) {
        User user = userRepository
                .findNotDisabledByEmail(email.trim())
                .orElseThrow(() -> new NotFoundException("No user found with the given email"));

        String rawToken = createAndSavePasswordResetToken(user);
        eventPublisher.publishPasswordResetRequestedEvent(
                new PasswordResetRequestedEvent(
                        user.getEmail(),
                        rawToken,
                        LocalDateTime.now().plusMinutes(minutesToExpire)
                )
        );
    }

    @Override @Transactional
    public void resetPassword(PasswordResetRequest request) {
        PasswordResetToken token = resetTokenRepository.findUsableToken(TokenUtils.hashToken(request.getToken()), LocalDateTime.now())
                .orElseThrow(() -> new NotFoundException("No valid token found, weather it's wrong, used, or expired"));

        userRepository.updatePassword(token.getUser().getId(), passwordEncoder.encode(request.getNewPassword()));

        token.setUsedAt(LocalDateTime.now());
        resetTokenRepository.save(token);

        userSessionRepository.deactivateSessionsForUser(token.getUser().getId());
    }

    @Transactional
    private LoginResponse login(User user) {
        String refreshToken = TokenUtils.generateRandomToken();

        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setToken(TokenUtils.hashToken(refreshToken));
        userSession.setCreatedAt(LocalDateTime.now());
        userSession.setExpiresAt(LocalDateTime.now().plusDays(expirationTime));

        userSessionRepository.save(userSession);

        ApplicationUser appUser = AuthMapper.toApplicationUser(user);
        String accessToken = jwtUtils.generateJwtToken(appUser);

        return new LoginResponse(appUser, accessToken, refreshToken);
    }

    private String createAndSavePasswordResetToken(User user) {
        String rawToken = TokenUtils.generateRandomToken();
        String tokenHash = TokenUtils.hashToken(rawToken);

        PasswordResetToken token = new PasswordResetToken();
        token.setCreatedAt(LocalDateTime.now());
        token.setUser(user);
        token.setToken(tokenHash);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(minutesToExpire));
        resetTokenRepository.save(token);
        return rawToken;
    }
}
