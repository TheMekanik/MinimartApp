package com.minimart.api.filter;

import com.minimart.api.entity.Users;
import com.minimart.api.repository.UsersRepository;
import com.minimart.api.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JWTTokenValidationFilter extends OncePerRequestFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final List<String> publicPaths;
    private final JwtUtil jwtUtil;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final UsersRepository usersRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            try {
                String jwt = authHeader.substring(7);

                Claims claims = Jwts.parser().verifyWith(jwtUtil.getSecretKey()).build().parseSignedClaims(jwt).getPayload();

                String username = claims.getSubject();
                List<String> roles = claims.get("roles", List.class);
                List<String> permissions = claims.get("permissions", List.class);
                Long id = claims.get("id", Long.class);

                Users user = usersRepository.findById(id).orElseThrow(null);

                if (!user.getAccessToken().equals(jwt) || user.getAccessToken() == null) {
                    throw new BadCredentialsException("Access denied.");
                }

                List<GrantedAuthority> authorities = new ArrayList<>();

                if (roles != null) {
                    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role))); // kontrak authority
                }

                if (permissions != null) {
                    permissions.forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm)));
                }

                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException exception) {
                authenticationEntryPoint.commence(
                        request, response, new BadCredentialsException("Token expired", exception)
                );

                return;
            } catch (Exception exception) {
                authenticationEntryPoint.commence(
                        request, response, new BadCredentialsException("Token invalid", exception)
                );
                return;
            }

        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return publicPaths.stream().anyMatch(publicPath -> antPathMatcher.match(publicPath, path));
    }
}
