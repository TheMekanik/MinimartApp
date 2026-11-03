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
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;

    @Override
    public LoginResponseDto login(LoginRequestDto request) {

        String loginIdentifier = request.username();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginIdentifier, request.password()
        ));

        var dto = new LoginResponseDto();
        var loggedInUser = (Users) authentication.getPrincipal();
        BeanUtils.copyProperties(loggedInUser, dto);

        Users user = usersRepository.findById(loggedInUser.getId()).orElseThrow(null);

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

        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(jwtUtil.getRefreshExpiry()));
        user.setLastLoginAt(Instant.now());

        dto.setAccess_token(accessToken);
        dto.setRefresh_token(refreshToken);

        return dto;
    }

    @Override
    public RefreshTokenResponseDto refreshToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String jwt = authHeader.substring(7);

            Claims claims = Jwts.parser().verifyWith(jwtUtil.getSecretKey()).build().parseSignedClaims(jwt).getPayload();
            Long id = claims.get("id", Long.class);

            Users existingUser = usersRepository.findById(id).orElseThrow(
                    () -> new AppException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "User not found.")
            );

            String existingRefreshToken = existingUser.getRefreshToken();

            if (existingRefreshToken == null || existingUser.getRefreshTokenExpiry().isBefore(Instant.now())) {
                existingUser.setRefreshToken(null);
                existingUser.setRefreshTokenExpiry(null);
                usersRepository.save(existingUser);
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
        } catch (BadCredentialsException ex) {
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
            String authHeader = request.getHeader("Authorization");
            String jwt = authHeader.substring(7);

            Claims claims = Jwts.parser().verifyWith(jwtUtil.getSecretKey()).build().parseSignedClaims(jwt).getPayload();
            Long id = claims.get("id", Long.class);

            Users loggedInUser = usersRepository.findById(id).orElseThrow(
                    () -> new AppException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "User not found.")
            );

            loggedInUser.setAccessToken(null);
            loggedInUser.setRefreshToken(null);
            loggedInUser.setRefreshTokenExpiry(null);

            usersRepository.save(loggedInUser);
        } catch (BadCredentialsException ex) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Invalid token"
            );
        }
    }

}
