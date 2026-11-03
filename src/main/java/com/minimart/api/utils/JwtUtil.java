package com.minimart.api.utils;

import com.minimart.api.entity.Users;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final Environment environment;

    @Getter
    private SecretKey secretKey;
    private Long expiry;

    @Getter
    private long refreshExpiry;

    @PostConstruct
    public void init() {
        String secret = environment.getProperty("jwt.secret");

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("credentials tidak valid/ditemukan");
        }

        String expiration = environment.getProperty("jwt.expiration", "3600000");
        String refreshExpiration = environment.getProperty("jwt.refresh-expiration", "604800000");

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiry = Long.parseLong(expiration);
        this.refreshExpiry = Long.parseLong(refreshExpiration);
    }

    public String generateJwtToken(Authentication authentication) {
        String jwt = "";

        List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(auth -> auth.startsWith("ROLE_")).toList();
        List<String> permisssions = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(auth -> !auth.startsWith("ROLE_")).toList();

        Date now = new Date();

        Date expirationDate = new Date(now.getTime() + this.expiry);

        Users fetchedUser = (Users) authentication.getPrincipal();
        jwt = Jwts.builder().issuer("minimart").subject(fetchedUser.getUsername())
                .claim("id", fetchedUser.getId())
                .claim("username", fetchedUser.getUsername())
                .claim("email", fetchedUser.getEmail())
                .claim("roles", roles)
                .claim("permissions", permisssions)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey).compact();

        return jwt;
    }

    public String generateRefreshToken(Authentication authentication) {
        String refreshToken = "";

        List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(auth -> auth.startsWith("ROLE_")).toList();
        List<String> permisssions = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).filter(auth -> !auth.startsWith("ROLE_")).toList();

        Date now = new Date();

        Date expirationDate = new Date(now.getTime() + this.refreshExpiry);

        Users fetchedUser = (Users) authentication.getPrincipal();
        refreshToken = Jwts.builder().issuer("bpiuniform").subject(fetchedUser.getUsername())
                .claim("id", fetchedUser.getId())
                .claim("username", fetchedUser.getUsername())
                .claim("email", fetchedUser.getEmail())
                .claim("roles", roles)
                .claim("permissions", permisssions)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey).compact();
        return refreshToken;
    }
}
