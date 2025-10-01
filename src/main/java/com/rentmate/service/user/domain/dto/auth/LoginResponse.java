package com.rentmate.service.user.domain.dto.auth;

import com.rentmate.service.user.domain.dto.user.UserProfileResponse;

public record LoginResponse(UserProfileResponse user, String accessToken, String refreshToken) {

}
