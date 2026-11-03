package com.minimart.api.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoginResponseDto {
    private Long user_id;
    private String username;
    private String email;
    private String access_token;
    private String refresh_token;
    private List<String> roles;
    private List<String> permissions;
}
