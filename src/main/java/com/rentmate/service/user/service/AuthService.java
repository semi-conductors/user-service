package com.rentmate.service.user.service;


import com.rentmate.service.user.domain.dto.auth.*;


public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void logout(String refreshToken);
    RefreshResponse refresh(String refreshToken);
    void sendResetToken(String email);
    void resetPassword(PasswordResetRequest request);
}
