package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.jwt.JwtAuthEntryPoint;
import com.example.demo.jwt.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] ALLOWED_URLS =
    {"/swagger-ui/**", "/v3/api-docs/**", "/error", "/api/auth/**", "/api/auth/login"};
    private final JwtAuthFilter jFilter;
    private final JwtAuthEntryPoint jPoint;

    @Bean
    AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration
    ) throws Exception {
       return configuration.getAuthenticationManager(); 
    }

    @Bean
    SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.authorizeHttpRequests(authorize -> {
            // ✅ ПРАВА ДОСТУПА:
            authorize.requestMatchers("/api/auth/**").permitAll();
            authorize.requestMatchers(ALLOWED_URLS).permitAll();
            // Devices - USER может только читать, ADMIN - все
            authorize.requestMatchers(HttpMethod.GET, "/api/devices", "/api/devices/**")
                    .hasAnyAuthority("DEVICE:READ", "DEVICE:WRITE");
            authorize.requestMatchers(HttpMethod.POST, "/api/devices").hasAuthority("DEVICE:WRITE");
            authorize.requestMatchers(HttpMethod.PUT, "/api/devices/**").hasAuthority("DEVICE:WRITE");
            authorize.requestMatchers(HttpMethod.DELETE, "/api/devices/**").hasAuthority("DEVICE:WRITE");
            
            // Rooms - USER может только читать СВОИ, ADMIN - все
            authorize.requestMatchers(HttpMethod.GET, "/api/rooms", "/api/rooms/**")
                    .hasAnyAuthority("ROOM:READ", "ROOM:WRITE");
            authorize.requestMatchers(HttpMethod.POST, "/api/rooms").hasAuthority("ROOM:WRITE");
            authorize.requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAuthority("ROOM:WRITE");
            authorize.requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAuthority("ROOM:WRITE");
            
            // Control - только WRITE права
            authorize.requestMatchers("/api/control/**").hasAuthority("DEVICE:WRITE");
            
            // Modes - только контроль
            authorize.requestMatchers("/api/modes/**").hasAuthority("MODE:CONTROL");
            
            // Users - только админ
            authorize.requestMatchers("/api/users/**").hasAuthority("USER:WRITE");
            
            // Mode rules - только админ
            authorize.requestMatchers("/api/mode-rules/**").hasAuthority("DEVICE:WRITE");
            
            authorize.requestMatchers(HttpMethod.GET, "/api/files", "/api/files/**").hasAnyAuthority("FILE:READ");
            authorize.requestMatchers(HttpMethod.POST, "/api/files").hasAuthority("FILE:WRITE");
            authorize.requestMatchers(HttpMethod.DELETE, "/api/files/**").hasAuthority("FILE:WRITE");

            authorize.anyRequest().authenticated();
        });

        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS
        ));
        httpSecurity.exceptionHandling(exception -> exception.authenticationEntryPoint(jPoint));
        httpSecurity.addFilterBefore(jFilter, UsernamePasswordAuthenticationFilter.class);
        
        return httpSecurity.build();
    }

    @Bean
    static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
