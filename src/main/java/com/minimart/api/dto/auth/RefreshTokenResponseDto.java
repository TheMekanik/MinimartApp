package com.minimart.api.dto.auth;

public record RefreshTokenResponseDto(
        String access_token,
        String refresh_token
) {
}
