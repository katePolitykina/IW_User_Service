package org.example.userservice.service;

import org.example.userservice.dto.CardDTO.CardRequestDTO;
import org.example.userservice.dto.CardDTO.CardResponseDTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.mapper.CardMapper;
import org.example.userservice.model.Card;
import org.example.userservice.model.User;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getById_shouldReturnCard_whenExists() {
        Card card = new Card();
        CardResponseDTO dto = new CardResponseDTO();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toCardResponseTo(card)).thenReturn(dto);

        CardResponseDTO result = cardService.getById(1L);

        assertEquals(dto, result);
    }

    @Test
    void getById_shouldThrow_whenNotExists() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cardService.getById(1L));
    }

    @Test
    void getByIds_shouldReturnList() {
        Card card1 = new Card();
        Card card2 = new Card();
        CardResponseDTO dto1 = new CardResponseDTO();
        CardResponseDTO dto2 = new CardResponseDTO();
        when(cardRepository.getByIds(List.of(1L, 2L))).thenReturn(Stream.of(card1, card2));
        when(cardMapper.toCardResponseTo(card1)).thenReturn(dto1);
        when(cardMapper.toCardResponseTo(card2)).thenReturn(dto2);

        List<CardResponseDTO> result = cardService.getByIds(List.of(1L, 2L));

        assertEquals(List.of(dto1, dto2), result);
    }

    @Test
    void getByIds_shouldReturnEmptyList_whenNoCards() {
        when(cardRepository.getByIds(List.of(1L, 2L))).thenReturn(Stream.empty());

        List<CardResponseDTO> result = cardService.getByIds(List.of(1L, 2L));
        assertTrue(result.isEmpty());
    }

    @Test
    void create_shouldReturnCard_whenUserExists() {
        CardRequestDTO req = new CardRequestDTO();
        req.setUserId(1L);
        Card card = new Card();
        Card saved = new Card();
        CardResponseDTO dto = new CardResponseDTO();
        when(cardMapper.toCard(req)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(saved);
        when(cardMapper.toCardResponseTo(saved)).thenReturn(dto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));


        CardResponseDTO result = cardService.create(req);

        assertEquals(dto, result);
    }

    @Test
    void create_shouldThrow_whenUserNotExists() {
        CardRequestDTO req = new CardRequestDTO();
        req.setUserId(1L);
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(BadRequestException.class, () -> cardService.create(req));
    }

    @Test
    void delete_shouldEvictCacheAndDeleteCard() {
        Card card = new Card();
        User user = new User();
        user.setId(42L);
        card.setUser(user);
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("userWithCards")).thenReturn(cache);

        cardService.delete(100L);

        verify(cardRepository).deleteById(100L);
        verify(cache).evict(42L);
    }

    @Test
    void delete_shouldNotEvictCacheIfCacheIsNull() {
        Card card = new Card();
        User user = new User();
        user.setId(13L);
        card.setUser(user);
        when(cardRepository.findById(99L)).thenReturn(Optional.of(card));
        when(cacheManager.getCache("userWithCards")).thenReturn(null);

        cardService.delete(99L);

        verify(cardRepository).deleteById(99L);
    }

    @Test
    void delete_shouldThrow_whenCardNotFound() {
        when(cardRepository.findById(200L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cardService.delete(200L));
    }
}