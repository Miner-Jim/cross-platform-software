package com.example.demo.jwt;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.enums.TokenType;
import com.example.demo.model.Token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    public Token generateAccessToken(
        Map<String, Object> extractClaims,
        Long duration,
        TemporalUnit duartionType,
        UserDetails user
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plus(duration, duartionType);
        String token = Jwts.builder().setClaims(extractClaims)
        .setSubject(user.getUsername())
        .setIssuedAt(toDate(now))
        .setExpiration(toDate(expiryDate))
        .signWith(decodeSecretKey(secret), SignatureAlgorithm.HS256).compact();
        return new Token(TokenType.ACCESS, token, expiryDate, false, null);
    }

    public Token generateRefreshToken(
        Long duration,
        TemporalUnit duartionType,
        UserDetails user
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plus(duration, duartionType);
        String token = Jwts.builder()
        .setSubject(user.getUsername())
        .setIssuedAt(toDate(now))
        .setExpiration(toDate(expiryDate))
        .signWith(decodeSecretKey(secret), SignatureAlgorithm.HS256).compact();
        return new Token(TokenType.REFRESH, token, expiryDate, false, null);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
        .setSigningKey(decodeSecretKey(secret))
        .build()
        .parseClaimsJws(token)
        .getBody();
    }

    private Date toDate(LocalDateTime time) {
        return Date.from(time.toInstant(ZoneOffset.UTC));
    }

    private Key decodeSecretKey(String secret) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public LocalDateTime getExpirnyDate(String token) {
        return toLocalDateTime(extractClaim(token, Claims::getExpiration));
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }

    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token) {
        if (token == null) return false;
        try {
            Jwts.parserBuilder().setSigningKey(decodeSecretKey(secret))
            .build().parseClaimsJws(token);
            return true;
        }
    catch (JwtException e) {return false;}
    }
}
