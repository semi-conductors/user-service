package com.rentmate.service.user.domain.dto.user;

import com.rentmate.service.user.domain.enumuration.UserRole;

public record UpdateProfileRoleRequest(UserRole role) {
}
