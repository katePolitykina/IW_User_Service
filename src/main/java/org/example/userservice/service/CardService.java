package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.CardTO.CardRequestTO;
import org.example.userservice.dto.CardTO.CardResponseTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.mapper.CardMapper;
import org.example.userservice.model.Card;
import org.example.userservice.model.User;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;

    public CardResponseTO getById(Long id) {
        return cardRepository
                .findById(id)
                .map(cardMapper::toCardResponseTo)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));
    }

    public List<CardResponseTO> getByIds(List<Long> ids) {
        return cardRepository
                .getByIds(ids)
                .map(cardMapper::toCardResponseTo)
                .toList();
    }

    public CardResponseTO create(CardRequestTO input) {
        if (userRepository.existsById(input.getUserId())) {
            return cardMapper.toCardResponseTo(cardRepository.save(cardMapper.toCard(input)));
        }else {
            throw new BadRequestException("User with id " + input.getUserId() + " does not exist");
        }
    }
    @Transactional
    public void delete(Long id) {
        if(cardRepository.existsById(id)) {
            cardRepository.deleteById(id);
        }else {
            throw new EntityNotFoundException("Card with id " + id + " not found");
        }
    }

}