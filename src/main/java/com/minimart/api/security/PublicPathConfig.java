package com.minimart.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PublicPathConfig {

    @Bean
    public List<String> publicPaths() {
        return List.of(
                "/api/v1/auth/login"
        );
    }

}
