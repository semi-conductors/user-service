package com.rentmate.service.user.domain.dto.auth;

public record LoginResponse(ApplicationUser user, String accessToken, String refreshToken) {

}
