package com.profitcalculator.dascher.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security setup for a stateless REST API.
 *
 * <p>No authentication is required — all API, Swagger, H2, and Actuator paths are open. CSRF is
 * disabled because we're stateless (no sessions, no cookies for auth).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsConfigurationSource corsConfigurationSource;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
                    .permitAll()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**"))
                    .permitAll()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/**"))
                    .permitAll()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**"))
                    .permitAll()
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**"))
                    .permitAll()
                    // denyAll() rather than authenticated() — a forgotten security rule returns a
                    // visible 403 instead of silently exposing the endpoint.
                    .anyRequest()
                    .denyAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
