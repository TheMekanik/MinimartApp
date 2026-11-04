package com.minimart.api.security;

import com.minimart.api.dto.ApiResponseDto;
import com.minimart.api.filter.JWTTokenValidationFilter;
import com.minimart.api.filter.AuditRequestLoggingFilter;
import com.minimart.api.repository.UsersRepository;
import com.minimart.api.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final List<String> publicPaths;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final AuditRequestLoggingFilter auditRequestLoggingFilter;
    @Value("${minimart.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests((request) -> {
                    publicPaths.forEach(path ->
                            request.requestMatchers(path).permitAll());
                    request.requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-resources/**"
                    ).permitAll();
                    request.anyRequest().authenticated();
                })
                .addFilterBefore(new JWTTokenValidationFilter(publicPaths, jwtUtil, authenticationEntryPoint, usersRepository), BasicAuthenticationFilter.class)
                .addFilterAfter(auditRequestLoggingFilter, JWTTokenValidationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
           response.setStatus(HttpStatus.UNAUTHORIZED.value());
           response.setContentType(MediaType.APPLICATION_JSON_VALUE);

           objectMapper.writeValue(
                   response.getOutputStream(),
                   ApiResponseDto.<Void>builder()
                           .code(HttpStatus.UNAUTHORIZED.value())
                           .message(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                           .details("Access not granted")
                           .build()
           );
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    ApiResponseDto.<Void>builder()
                            .code(HttpStatus.FORBIDDEN.value())
                            .message(HttpStatus.FORBIDDEN.getReasonPhrase())
                            .message("Access forbidden")
            );
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList()
        );
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
