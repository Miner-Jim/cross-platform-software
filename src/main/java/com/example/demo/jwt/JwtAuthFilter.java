package com.example.demo.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter{

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    @Value("${jwt.access.name}")
    private String accessCookieName;

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = "";
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {  // ← ДОБАВЬ ЭТУ ПРОВЕРКУ!
            for (Cookie cookie: cookies) {
                if (accessCookieName.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if(token.equals("") || !tokenProvider.validateToken(token)) {
            logger.debug("The JWT token was not found for: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String username = tokenProvider.getUsername(token);

        if(username == null) {
            logger.warn("Couldn't extract username from token");
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails user = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        logger.debug("The user is authenticated: {} for {}", username, request.getRequestURI());
        filterChain.doFilter(request, response);
    }

}
