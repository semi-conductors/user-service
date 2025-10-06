package com.rentmate.service.user.domain.dto.user;

import lombok.Data;

@Data
public class UserPrincipal {
    private String username;
    private Long id;
    private String email;
    private String role;

    public UserPrincipal(String username, Long id, String email, String role) {
        this.username = username;
        this.id = id;
        this.email = email;
        this.role = role;
    }
}
