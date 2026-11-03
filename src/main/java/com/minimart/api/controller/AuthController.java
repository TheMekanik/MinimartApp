package com.minimart.api.controller;

import com.minimart.api.dto.ApiResponseDto;
import com.minimart.api.dto.auth.LoginRequestDto;
import com.minimart.api.dto.auth.LoginResponseDto;
import com.minimart.api.dto.auth.RefreshTokenResponseDto;
import com.minimart.api.service.IAuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {

        LoginResponseDto login = authService.login(request);

        return ResponseEntity.ok().body(
                ApiResponseDto.<LoginResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .data(login)
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDto<RefreshTokenResponseDto>> refreshToken(HttpServletRequest request) {

        RefreshTokenResponseDto newToken = authService.refreshToken(request);

        return ResponseEntity.ok().body(
                ApiResponseDto.<RefreshTokenResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .data(newToken)
                        .build()
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpServletRequest request) {
        authService.logout(request);

        return ResponseEntity.ok().body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message(HttpStatus.OK.getReasonPhrase())
                        .details("Logout successful")
                        .build()
        );
    }

}
