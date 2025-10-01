package com.rentmate.service.user.service.shared.util;

import com.rentmate.service.user.domain.dto.auth.ApplicationUser;
import com.rentmate.service.user.domain.dto.user.UserProfileResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {
    private SecretKey key;
    private long expirationTime;

    public JwtUtils(@Value(value = "${jwt.secret-key:this-is-not-a-key-at-all}") String secret,
                    @Value(value = "${jwt.expiration-time-minutes:60}")long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = jwtExpirationMs;
    }

    public String generateJwtToken(UserProfileResponse user) {
        return Jwts.builder()
                .subject(user.id().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * expirationTime))
                .claim("role", "ROLE_"+user.role())
                .claim("username", user.username())
                .signWith(key)
                .compact();
    }

//    public String getUsernameFromJwtToken(String token) {
//        return Jwts.parser()
//                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject(); //subject claim is the username, 'sub' in JWT
//    }


    public boolean validateJwtToken(String authToken) {
        Jwts.parser().verifyWith(key);
        return true;
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().
                verifyWith(key).
                build().
                parseSignedClaims(token).
                getPayload();
    }
}