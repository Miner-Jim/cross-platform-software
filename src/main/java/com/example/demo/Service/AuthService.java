package com.example.demo.service;



import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ChangePasswordDto;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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
    private final PasswordEncoder passwordEncoder;

    private void addAccessTokenCookie(HttpHeaders headers, Token token) {
        headers.add(HttpHeaders.SET_COOKIE, 
        cookieUtil.createAccessTokenCookie(token.getValue(), accessTokenDurationSecond).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders headers, Token token) {
    headers.add(HttpHeaders.SET_COOKIE, 
    cookieUtil.createRefreshTokenCookie(token.getValue(), refreshTokenDurationSecond).toString()); 
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
        logger.info("User Login Attempt: {}", loginRequest.username());
        try {
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
        } catch (AuthenticationException e) {
            logger.warn("Authentication error for the user: {}", loginRequest.username());
            throw e;
        }
    }

    public ResponseEntity<LoginResponse> refresh(String refresToken) {
        logger.debug("Token Update Request");
        if (!jwtTokenProvider.validateToken(refresToken)) {
            logger.warn("Invalid refresh token");
            throw new RuntimeException("Invalid refresh token");
        }
        User user = userService.getUserByUsername(jwtTokenProvider.getUsername(refresToken));
        HttpHeaders headers = new HttpHeaders();
        Token newAccess = jwtTokenProvider.generateAccessToken(Map.of("role", user.getRole().getAuthority()),
                accessTokenDurationMinute, ChronoUnit.MINUTES, user);
        addAccessTokenCookie(headers, newAccess);
        logger.info("The token has been updated for the user: {}", user.getUsername());
        return ResponseEntity.ok().headers(headers).body(new LoginResponse(true, user.getRole().getName()));
    }

    public ResponseEntity<LoginResponse> logout(String accessToken) {
        String username = jwtTokenProvider.getUsername(accessToken);
        logger.info("User Output: {}", username);
        SecurityContextHolder.clearContext();
        User user = userService.getUserByUsername(jwtTokenProvider.getUsername(accessToken));
        revokeAllTokensOfUser(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, 
        cookieUtil.deleteAccessTokenCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, 
        cookieUtil.deleteRefreshTokenCookie().toString());
        return ResponseEntity.ok().headers(headers).body(new LoginResponse(false, null));

    }

    public ResponseEntity<String> changePassword(ChangePasswordDto changePasswordDto, String username) {
        logger.info("Changing password for user: {}", username);
        
        try {
            // Проверяем что новый пароль и подтверждение совпадают
            if (!changePasswordDto.newPassword().equals(changePasswordDto.confirmPassword())) {
                logger.warn("Password confirmation mismatch for user: {}", username);
                return ResponseEntity.badRequest().body("New password and confirmation do not match");
            }
            
            User user = userService.getUserByUsername(username);
            if (user == null) {
                logger.warn("User not found: {}", username);
                return ResponseEntity.badRequest().body("User not found");
            }
            
            // Проверяем текущий пароль
            if (!passwordEncoder.matches(changePasswordDto.currentPassword(), user.getPassword())) {
                logger.warn("Current password is incorrect for user: {}", username);
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }
            
            // Проверяем что новый пароль не совпадает со старым
            if (passwordEncoder.matches(changePasswordDto.newPassword(), user.getPassword())) {
                logger.warn("New password cannot be the same as current password for user: {}", username);
                return ResponseEntity.badRequest().body("New password must be different from current password");
            }
            
            // Устанавливаем новый пароль
            user.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
            userService.updateUser(user.getId(), user); 
            
            logger.info("Password successfully changed for user: {}", username);
            return ResponseEntity.ok("Password changed successfully");
            
        } catch (Exception e) {
            logger.error("Error changing password for user {}: {}", username, e.getMessage());
            return ResponseEntity.internalServerError().body("Error changing password");
        }
}

}
