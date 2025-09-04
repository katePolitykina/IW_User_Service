package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.CardDTO.CardRequestDTO;
import org.example.userservice.dto.CardDTO.CardResponseDTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.exception.ForbiddenException;
import org.example.userservice.mapper.CardMapper;
import org.example.userservice.model.Card;
import org.example.userservice.model.User;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.security.AuthInfo;
import org.example.userservice.security.SecurityService;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;
    private final AuthInfo authInfo;
    private CacheManager cacheManager;
    private SecurityService securityService;

    public CardResponseDTO getById(Long id) {
        Card card = cardRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));

        if ( !securityService.isOwner(card.getUser().getId()) ){
            throw new ForbiddenException("You do not have permission to access this card");
        }

        return cardMapper.toCardResponseTo(card);
    }
    public List<CardResponseDTO> getByIds(List<Long> ids) {

        return cardRepository
                .getByIds(ids)
                .peek(card -> {
                    if (!securityService.isOwner(card.getUser().getId())) {
                        throw new ForbiddenException("You do not have permission to access card " + card.getId());
                    }
                })
                .map(cardMapper::toCardResponseTo)
                .toList();
    }

    public CardResponseDTO create(CardRequestDTO input) {
        Long userId = authInfo.getUserId();

        if(userId == null) {
            throw new BadRequestException("Missing userId in request headers");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User with id " + userId + " does not exist"));
        Card card = cardMapper.toCard(input);
        card.setUser(user);
        Card savedCard = cardRepository.save(card);

        Optional.ofNullable(cacheManager.getCache("userWithCards"))
                .ifPresent(cache -> cache.evict(userId));

        return cardMapper.toCardResponseTo(savedCard);
    }

    @Transactional
    public void delete(Long id) {

        var card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));

        Long userId = card.getUser().getId();

        if ( ! ( securityService.isOwner(userId)|| securityService.hasRole("ROLE_iw.admin") ) ){
            throw new ForbiddenException("You do not have permission to delete this card");
        }

        cardRepository.deleteById(id);

        Optional.ofNullable(cacheManager.getCache("userWithCards"))
                .ifPresent(cache -> cache.evict(userId));

    }

}