package com.example.demo.jwt;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    @Value("${jwt.access.name}")
    private String accessName;
    @Value("${jwt.refresh.name}")
    private String refreshName;

    public HttpCookie createAccessTokenCookie(String token, long duration) {
        return ResponseCookie.from(accessName, token)
        .maxAge(duration)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite("None")
        .build();
        
    }

    public HttpCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(accessName, "")
        .maxAge(0)
        .httpOnly(true)
        .path("/")
        .build();
    }

    public HttpCookie createRefreshTokenCookie(String token, long duration) {
        return ResponseCookie.from(refreshName, token)
        .maxAge(duration)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite("None")
        .build();
    }

    // public HttpCookie deleteRefreshTokenCookie() {
    //     return ResponseCookie.from(refreshName, "")
    //     .maxAge(0)
    //     .httpOnly(true)
    //     .path("/")
    //     .build();
    // }
}
