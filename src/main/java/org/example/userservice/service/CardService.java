package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.CardDTO.CardRequestDTO;
import org.example.userservice.dto.CardDTO.CardResponseDTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.mapper.CardMapper;
import org.example.userservice.model.Card;
import org.example.userservice.model.User;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;
    private CacheManager cacheManager;

    public CardResponseDTO getById(Long id, Authentication authentication) {
        Card card = cardRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));
        if (!card.getUser().getEmail().equals(authentication.getName()) ){
            throw new AccessDeniedException("Access denied");
        }
        return cardMapper.toCardResponseTo(card);
    }

    public List<CardResponseDTO> getByIds(List<Long> ids, Authentication authentication) {
        List<Card>  cards = cardRepository.getByIds(ids).toList();
        boolean allMatch = cards.stream()
                .allMatch(card -> card.getUser().getEmail().equals(authentication.getName()));
        if (!allMatch) {
            throw new AccessDeniedException("Access denied: you can only access your own cards.");
        }

        return cards.stream()
                .map(cardMapper::toCardResponseTo)
                .toList();
    }

    @CacheEvict(value = "userWithCards", key = "#input.userId")
    public CardResponseDTO create(CardRequestDTO input, Authentication authentication) {
        User user = userRepository.findUserByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Access denied: you need to create user before creating cards."));

        if (!user.getId().equals(input.getUserId())) {
            throw new AccessDeniedException("Access denied: you can only create your own cards.");
        }
        Card card = cardMapper.toCard(input);
        card.setUser(user);
        Card savedCard = cardRepository.save(card);
        return cardMapper.toCardResponseTo(savedCard);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        var card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));
        if (!card.getUser().getEmail().equals(authentication.getName())) {
            throw new AccessDeniedException("Access denied: you can only delete your own cards.");
        }
        Long userId = card.getUser().getId();

        cardRepository.deleteById(id);

        Optional.ofNullable(cacheManager.getCache("userWithCards"))
                .ifPresent(cache -> cache.evict(userId));

    }

}