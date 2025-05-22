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
import org.example.userservice.repository.CardRepo;
import org.example.userservice.repository.UserRepo;
import org.springframework.stereotype.Service;

import javax.management.BadAttributeValueExpException;
import java.util.List;

@AllArgsConstructor
@Service
public class CardService {
    private final CardRepo cardRepo;
    private final CardMapper cardMapper;
    private final UserRepo userRepo;

    public CardResponseTO get(Long id) {
        return cardRepo
                .findById(id)
                .map(cardMapper::toCardResponseTo)
                .orElseThrow(() -> new EntityNotFoundException("Card with id " + id + " not found"));
    }

    public List<CardResponseTO> get(List<Long> ids) {
        return cardRepo
                .getByIds(ids)
                .map(cardMapper::toCardResponseTo)
                .toList();
    }

    public CardResponseTO create(CardRequestTO input) {
        if (userRepo.existsById(input.getUserId())) {
            return cardMapper.toCardResponseTo(cardRepo.save(cardMapper.toCard(input)));
        }else {
            throw new BadRequestException("User with id " + input.getUserId() + " does not exist");
        }
    }
    @Transactional
    public void delete(Long id) {
        if(cardRepo.existsById(id)) {
            cardRepo.deleteById(id);
        }else {
            throw new EntityNotFoundException("Card with id " + id + " not found");
        }
    }

    @Transactional
    public CardResponseTO update(Long id, CardRequestTO input) {
        Card card = cardMapper.toCard(input);

        User user = userRepo.findById(input.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with id " + input.getUserId() + " not found"));

        card.setUser(user);

        return cardRepo
                .update(card)
                .map(cardMapper::toCardResponseTo)
                .orElseThrow();

    }

}