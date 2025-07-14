package org.example.userservice.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.CardDTO.CardRequestDTO;
import org.example.userservice.dto.CardDTO.CardResponseDTO;
import org.example.userservice.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1.0/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping("/{id}")
    public CardResponseDTO getById(@PathVariable Long id, Authentication authentication) {
        return cardService.getById(id, authentication);
    }

    @GetMapping
    public List<CardResponseDTO> getByIds(@RequestParam @Size(min = 1) List<@NotNull Long> ids, Authentication authentication) {
        return cardService.getByIds(ids, authentication);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponseDTO create(@RequestBody @Valid CardRequestDTO input, Authentication authentication) {
        return cardService.create(input, authentication);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        cardService.delete(id, authentication);
    }

}