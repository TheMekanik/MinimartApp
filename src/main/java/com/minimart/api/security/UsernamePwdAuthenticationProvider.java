package com.minimart.api.security;

import com.minimart.api.entity.Permissions;
import com.minimart.api.entity.Roles;
import com.minimart.api.entity.Users;
import com.minimart.api.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UsernamePwdAuthenticationProvider implements AuthenticationProvider {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String pwd = authentication.getCredentials().toString();
        Users users = usersRepository.findByEmail(username).orElseGet(
                () -> usersRepository.findByUsername(username).orElseThrow(
                        () -> new UsernameNotFoundException(
                                "Nama pengguna tidak ditemukan : " + username
                        )
                )
        );

        if (!passwordEncoder.matches(pwd, users.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        Set<Roles> roles = users.getRoles();
        Set<GrantedAuthority> authoritiesSet = new HashSet<>();

        for (Roles role : users.getRoles()) {
            authoritiesSet.add(new SimpleGrantedAuthority(role.getRoleName()));

            for (Permissions permission : role.getPermissions()) {
                authoritiesSet.add(new SimpleGrantedAuthority(permission.getPermissionName()));
            }
        }

        List<GrantedAuthority> authorities = new ArrayList<>(authoritiesSet);

        return new UsernamePasswordAuthenticationToken(users,null,authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
