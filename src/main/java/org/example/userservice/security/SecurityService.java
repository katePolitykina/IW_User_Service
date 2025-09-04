package org.example.userservice.security;


import lombok.RequiredArgsConstructor;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("securityService")
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public boolean isOwnerByIdOrAdmin(Long userId, Authentication authentication) {
        String keycloakId = (String) authentication.getPrincipal();

        if (isAdmin(authentication)) {
            return true;
        }

        return userRepository.findById(userId)
                .map(user -> user.getKeycloakId().equals(keycloakId))
                .orElse(false);
    }


    public boolean isCardOwnerOrAdmin(Long cardId, Authentication authentication) {
        String keycloakId = (String) authentication.getPrincipal();

        if (isAdmin(authentication)) {
            return true;
        }
        return cardRepository.findById(cardId)
                .map(card -> card.getUser().getKeycloakId().equals(keycloakId))
                .orElse(false);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_iw.admin"));
    }
}
