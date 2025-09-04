package org.example.userservice.security;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityService {
    private final AuthInfo authInfo;

    public boolean isOwner(Long userId) {
        
        if (authInfo.getUserId() == null) {
            throw new ForbiddenException("Missing authentication headers");
        }
        log.warn("Checking if owner of user {}", authInfo.getUserId());
        return userId.equals(authInfo.getUserId());
    }

    public boolean hasRole(String role) {
        if (authInfo.getRoles() == null || authInfo.getRoles().isEmpty()) {
            return false;
        }
        return Arrays.asList(authInfo.getRoles().split(","))
                .stream()
                .anyMatch(r -> r.trim().equals(role));
    }
}
