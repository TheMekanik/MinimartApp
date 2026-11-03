package com.minimart.api.service;

import com.minimart.api.dto.auth.LoginRequestDto;
import com.minimart.api.dto.auth.LoginResponseDto;
import com.minimart.api.dto.auth.RefreshTokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthService {
    LoginResponseDto login(LoginRequestDto request);
    RefreshTokenResponseDto refreshToken(HttpServletRequest request);
    void logout(HttpServletRequest request);
}
