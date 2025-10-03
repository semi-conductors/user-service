package com.rentmate.service.user.domain.dto.user;

import lombok.Data;

@Data
public class UserEmailDto {
    private String email;

    public UserEmailDto(String email) {
        this.email = email;
    }
}
