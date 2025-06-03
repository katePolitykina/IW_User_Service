package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.CardDTO.CardRequestDTO;
import org.example.userservice.dto.CardDTO.CardResponseDTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.mapper.CardMapper;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
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

    public CardResponseDTO getById(Long id) {
        return cardRepository
                .findById(id)
                .map(cardMapper::toCardResponseTo)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));
    }

    public List<CardResponseDTO> getByIds(List<Long> ids) {
        return cardRepository
                .getByIds(ids)
                .map(cardMapper::toCardResponseTo)
                .toList();
    }

    @CacheEvict(value = "userWithCards", key = "#input.userId")
    public CardResponseDTO create(CardRequestDTO input) {
        if (userRepository.existsById(input.getUserId())) {
            return cardMapper.toCardResponseTo(cardRepository.save(cardMapper.toCard(input)));
        } else {
            throw new BadRequestException("User with id " + input.getUserId() + " does not exist");
        }
    }

    @Transactional
    public void delete(Long id) {
        var card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));

        Long userId = card.getUser().getId();

        cardRepository.deleteById(id);

        Optional.ofNullable(cacheManager.getCache("userWithCards"))
                .ifPresent(cache -> cache.evict(userId));

    }

}