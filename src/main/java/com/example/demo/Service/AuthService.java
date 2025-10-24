package com.example.demo.service;



import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserDto;
import com.example.demo.jwt.CookieUtil;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.model.Token;
import com.example.demo.model.User;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${jwt.access.duration.second}")
    private long accessTokenDurationSecond;
    @Value("${jwt.access.duration.minute}")
    private long accessTokenDurationMinute;

    @Value("${jwt.refresh.duration.second}")
    private long refreshTokenDurationSecond;
    @Value("${jwt.refresh.duration.day}")
    private long refreshTokenDurationDay;

    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final AuthenticationManager authenticationManager;

    private void addAccessTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE, 
        cookieUtil.createAccessTokenCookie(token.getValue(), accessTokenDurationSecond).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
    headers.add(HttpHeaders.SET_COOKIE, 
    cookieUtil.createRefreshTokenCookie(token.getValue(), refreshTokenDurationSecond).toString()); // Было accessTokenDurationSecond
    }

    private void revokeAllTokensOfUser(User user) {
        user.getTokens().forEach(t -> {
            if (t.getExpiryDate().isBefore(LocalDateTime.now())) {
                tokenRepository.delete(t);
            }
            else if (t.isDisabled()) {
                t.setDisabled(true);
                tokenRepository.save(t);
            }
        });
    }

    public ResponseEntity<LoginResponse> login(
        LoginRequest loginRequest,
        String access, 
        String refresh
    ) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        String username = loginRequest.username();
        User user = userService.getUserByUsername(username);
        boolean accessTokenValid = jwtTokenProvider.validateToken(access);
        boolean refreshTokenValid = jwtTokenProvider.validateToken(refresh);
        HttpHeaders headers = new HttpHeaders();
        Token newAccess, newRefresh;
        revokeAllTokensOfUser(user);
        if(!accessTokenValid) {
            newAccess = jwtTokenProvider.generateAccessToken(
            Map.of("role", user.getRole().getAuthority()),
            accessTokenDurationMinute, ChronoUnit.MINUTES, user);
            newAccess.setUser(user);
            addAccessTokenCookie(headers, newAccess);
        }
        if(!refreshTokenValid || accessTokenValid) {
            newRefresh = jwtTokenProvider.generateRefreshToken(refreshTokenDurationDay, ChronoUnit.DAYS, user);
            newRefresh.setUser(user);
            addRefreshTokenCookie(headers, newRefresh);
            tokenRepository.save(newRefresh);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok().headers(headers).body(new LoginResponse(true, user.getRole().getName()));
    }

}
