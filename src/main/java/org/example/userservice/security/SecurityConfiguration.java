package org.example.userservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final ClaimsFromHeaderFilter claimsFromHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

            .addFilterBefore(claimsFromHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers(HttpMethod.POST, "/api/v1.0/users").permitAll()
                            .anyRequest().authenticated()
                    );
        return http.build();
    }
}

