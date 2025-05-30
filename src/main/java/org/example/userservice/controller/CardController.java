package org.example.userservice.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.CardTO.CardRequestTO;
import org.example.userservice.dto.CardTO.CardResponseTO;
import org.example.userservice.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping("/{id}")
    public CardResponseTO getById(@PathVariable Long id){
        return cardService.getById(id);
    }

    @PostMapping("/by-ids")
    public List<CardResponseTO> getByIds(@RequestBody @Valid @Size(min = 1) List<@NotNull Long> ids){
        return cardService.getByIds(ids);

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponseTO create(@RequestBody @Valid CardRequestTO input){
        return cardService.create(input);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        cardService.delete(id);
    }

}