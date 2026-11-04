package com.minimart.api.service.impl;

import com.minimart.api.dto.auth.LoginRequestDto;
import com.minimart.api.dto.auth.LoginResponseDto;
import com.minimart.api.dto.auth.RefreshTokenResponseDto;
import com.minimart.api.entity.Permissions;
import com.minimart.api.entity.Roles;
import com.minimart.api.entity.Users;
import com.minimart.api.exception.AppException;
import com.minimart.api.repository.UsersRepository;
import com.minimart.api.service.IAuthService;
import com.minimart.api.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {

        String loginIdentifier = request.username();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginIdentifier, request.password()
        ));

        var loggedInUser = (Users) authentication.getPrincipal();
        Users user = usersRepository.findById(loggedInUser.getId()).orElseThrow(
                () -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "User is not available"
                )
        );
        var dto = new LoginResponseDto();
        dto.setUser_id(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();

        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .toList();

        dto.setRoles(roles);
        dto.setPermissions(permissions);

        String accessToken = jwtUtil.generateJwtToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        dto.setAccess_token(accessToken);
        dto.setRefresh_token(refreshToken);

        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(jwtUtil.getRefreshExpiry()));
        user.setLastLoginAt(Instant.now());

        usersRepository.save(user);
        log.info("User logged in successfully: userId={}", user.getId());

        return dto;
    }

    @Override
    @Transactional
    public RefreshTokenResponseDto refreshToken(HttpServletRequest request) {
        try {
            String jwt = extractBearerToken(request);

            Claims claims = Jwts.parser().verifyWith(jwtUtil.getSecretKey()).build().parseSignedClaims(jwt).getPayload();
            Long id = claims.get("id", Long.class);
            if (id == null || !"minimart".equals(claims.getIssuer())) {
                throw new AppException(
                        HttpStatus.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Invalid refresh token"
                );
            }

            Users existingUser = usersRepository.findById(id).orElseThrow(
                    () -> new AppException(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Invalid refresh token")
            );

            String existingRefreshToken = existingUser.getRefreshToken();

            if (existingUser.getDeletedAt() != null
                    || existingRefreshToken == null
                    || !existingRefreshToken.equals(jwt)
                    || existingUser.getRefreshTokenExpiry() == null
                    || existingUser.getRefreshTokenExpiry().isBefore(Instant.now())) {
                existingUser.setRefreshToken(null);
                existingUser.setRefreshTokenExpiry(null);
                usersRepository.save(existingUser);
                log.info("Access and refresh tokens rotated: userId={}", existingUser.getId());
                throw new AppException(
                        HttpStatus.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Your session has expired. Login to continue"
                );
            }

            Set<GrantedAuthority> authoritiesSet = new HashSet<>();

            if (existingUser.getRoles() != null) {
                for (Roles role : existingUser.getRoles()) {
                    authoritiesSet.add(new SimpleGrantedAuthority(role.getRoleName()));
                    if (role.getPermissions() != null) {
                        for (Permissions permission : role.getPermissions()) {
                            authoritiesSet.add(new SimpleGrantedAuthority(permission.getPermissionName()));
                        }
                    }
                }
            }

            List<GrantedAuthority> authorities = new ArrayList<>(authoritiesSet);
            Authentication authentication = new UsernamePasswordAuthenticationToken(existingUser, null, authorities);

            String newAccessToken = jwtUtil.generateJwtToken(authentication);
            String newRefreshToken = jwtUtil.generateRefreshToken(authentication);
            Instant newRefreshTokenExpiry = Instant.now().plusMillis(jwtUtil.getRefreshExpiry());

            existingUser.setAccessToken(newAccessToken);
            existingUser.setRefreshToken(newRefreshToken);
            existingUser.setRefreshTokenExpiry(newRefreshTokenExpiry);

            usersRepository.save(existingUser);

            return new RefreshTokenResponseDto(newAccessToken, newRefreshToken);
        } catch (JwtException | IllegalArgumentException | BadCredentialsException ex) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Invalid token"
            );
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        try {
            String jwt = extractBearerToken(request);

            Claims claims = Jwts.parser().verifyWith(jwtUtil.getSecretKey()).build().parseSignedClaims(jwt).getPayload();
            Long id = claims.get("id", Long.class);

            Users loggedInUser = usersRepository.findById(id).orElseThrow(
                    () -> new AppException(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Invalid token")
            );

            if (loggedInUser.getDeletedAt() != null
                    || !jwt.equals(loggedInUser.getAccessToken())) {
                throw new BadCredentialsException("Invalid token");
            }

            loggedInUser.setAccessToken(null);
            loggedInUser.setRefreshToken(null);
            loggedInUser.setRefreshTokenExpiry(null);

            usersRepository.save(loggedInUser);
            log.info("User logged out successfully: userId={}", loggedInUser.getId());
        } catch (JwtException | IllegalArgumentException | BadCredentialsException ex) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Invalid token"
            );
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            throw new BadCredentialsException("Invalid Authorization header");
        }
        return authHeader.substring(7).trim();
    }

}
