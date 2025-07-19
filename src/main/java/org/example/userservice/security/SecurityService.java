package org.example.userservice.security;


import lombok.RequiredArgsConstructor;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;

@Component("securityService")
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public boolean isOwnerByEmailOrAdmin(Long userId, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String emailFromToken = jwt.getClaimAsString("email");

        var roles = jwt.getClaimAsStringList("roles");
        if (roles != null && roles.contains("ROLE_iw.admin")) {
            return true;
        }

        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(emailFromToken))
                .orElse(false);
    }

    public boolean isOwnerByEmailOrAdmin(String userEmail, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String emailFromToken = jwt.getClaimAsString("email");

        var roles = jwt.getClaimAsStringList("roles");
        if (roles != null && roles.contains("ROLE_iw.admin")) {
            return true;
        }
        return userEmail.equals(emailFromToken);
    }
    public boolean isCardOwnerOrAdmin(Long cardId, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String emailFromToken = jwt.getClaimAsString("email");

        var roles = jwt.getClaimAsStringList("roles");
        if (roles != null && roles.contains("ROLE_iw.admin")) {
            return true;
        }
        return cardRepository.findById(cardId).map(card -> card.getUser().getEmail().equals(emailFromToken))
                .orElse(false);
    }
}
