package org.example.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.example.userservice.dto.CardDTO.CardRequestDTO;
import org.example.userservice.model.Card;
import org.example.userservice.model.User;
import org.example.userservice.repository.CardRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "ENCRYPTION_KEY=test-key"
})

class CardControllerIntegrationTest {
    private static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private MockMvc mockMvc;
    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private EncryptionUtil encryptionUtil ;

    @Autowired
    private CacheManager cacheManager;
    private static final String BASE_URL = "/api/v1.0/cards";




    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }
    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();
        Optional.ofNullable(cacheManager.getCache("userWithCards")).ifPresent(Cache::clear);
    }

    @Nested
    class getById {
        @Test
        @SneakyThrows
        void getById_ReturnsNotFound_whenNotFound()  {
            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isNotFound());
        }
        @SneakyThrows
        @Test
        void getById_ReturnsCard_WhenExists() {
            User user = new User();
            user.setName("John");
            user.setSurname("Doe");
            user.setEmail("john@example.com");
            user.setBirthDate(LocalDate.now().minusYears(30));
            user = userRepository.save(user);

            Card card = new Card();
            card.setNumber(encryptionUtil.encrypt("1234567812345678"));
            card.setUser(user);
            card.setHolder("John Doe");
            card.setExpirationDate(LocalDate.now().plusYears(1));
            card = cardRepository.save(card);

            mockMvc.perform(get("/api/v1.0/cards/{id}", card.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(card.getId()))
                    .andExpect(jsonPath("$.number").value("1234567812345678"))
                    .andExpect(jsonPath("$.userId").value(user.getId()))
                    .andExpect(jsonPath("$.holder").value("John Doe"));
        }


    }

    @Nested
    class getByIds {


        @Test
        @Transactional
        void getByIds_ReturnsCards_WhenSomeIdsExist() throws Exception {
            User user = new User();
            user.setName("John");
            user.setSurname("Doe");
            user.setEmail("john@example.com");
            user.setBirthDate(LocalDate.now().minusYears(30));
            user = userRepository.save(user);

            Card card1 = new Card();
            card1.setNumber(encryptionUtil.encrypt("1234567812345678"));
            card1.setUser(user);
            card1.setHolder("John Doe");
            card1.setExpirationDate(LocalDate.now().plusYears(1));
            card1 = cardRepository.save(card1);

            Card card2 = new Card();
            card2.setNumber(encryptionUtil.encrypt("1234567812345678"));
            card2.setUser(user);
            card2.setHolder("John Doe");
            card2.setExpirationDate(LocalDate.now().plusYears(2));
            card2 = cardRepository.save(card2);

            mockMvc.perform(get("/api/v1.0/cards")
                            .param("ids", card1.getId().toString(), card2.getId().toString(), String.valueOf(card1.getId() + card2.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(card1.getId()))
                    .andExpect(jsonPath("$[1].id").value(card2.getId()));
        }

        @Test
        void getByIds_ReturnsBadRequest_WhenIdsMissing() throws Exception {
            mockMvc.perform(get("/api/v1.0/cards?ids="))
                    .andExpect(status().isBadRequest());
        }

    }


    @Nested
    class create {
        @Test
        @SneakyThrows
        void create_ReturnsCreated_WhenInputIsValid() {
            User user = new User();
            user.setName("John");
            user.setSurname("Doe");
            user.setEmail("john@example.com");
            user.setBirthDate(LocalDate.now().minusYears(30));
            user = userRepository.save(user);

            CardRequestDTO dto = new CardRequestDTO();
            dto.setNumber("1234567812345678");
            dto.setUserId(user.getId());
            dto.setHolder("John Doe");
            dto.setExpirationDate(LocalDate.now().plusYears(2));

            mockMvc.perform(post("/api/v1.0/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.number").value(dto.getNumber()))
                    .andExpect(jsonPath("$.userId").value(user.getId()))
                    .andExpect(jsonPath("$.holder").value(dto.getHolder()));
            assertEquals(1, cardRepository.count());

        }

        @Test
        @SneakyThrows
        void create_ReturnsBadRequest_WhenUserDoesNotExist() {
            CardRequestDTO dto = new CardRequestDTO();
            dto.setNumber("1234567812345678");
            dto.setUserId(999L);
            dto.setHolder("John Doe");
            dto.setExpirationDate(LocalDate.now().plusYears(2));
            mockMvc.perform(post("/api/v1.0/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User with id 999 does not exist"));
        }
        @Test
        @SneakyThrows
        void create_ReturnsBadRequest_WhenInputIsInvalid() {
            CardRequestDTO dto = new CardRequestDTO();
            dto.setNumber("123");
            dto.setUserId(null);
            dto.setHolder("A");
            dto.setExpirationDate(LocalDate.now().minusYears(1));

            mockMvc.perform(post("/api/v1.0/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors.length()").value(4));
        }
    }

    @Nested
    class delete {
        @Test
        @SneakyThrows
        void delete_RemovesCardAndEvictsCache() {
            var user = new User();
            user.setName("Test");
            user.setSurname("User");
            user.setEmail("test@example.com");
            user.setBirthDate(LocalDate.of(2000, 1, 1));
            user = userRepository.save(user);

            var card = new Card();
            card.setNumber(encryptionUtil.encrypt("1234567812345678"));
            card.setUser(user);
            card.setHolder("Test Holder");
            card.setExpirationDate(LocalDate.now().plusYears(2));
            card = cardRepository.save(card);

            mockMvc.perform(delete(BASE_URL + "/{id}", card.getId()))
                    .andExpect(status().isOk());

            assertFalse(cardRepository.existsById(card.getId()));

            var id = user.getId();
            Optional.ofNullable(cacheManager.getCache("userWithCards"))
                    .ifPresent(cache -> assertNull(cache.get(id)));
        }

        @Test
        @SneakyThrows
        void delete_ThrowsException_WhenCardNotFound() {
            mockMvc.perform(delete(BASE_URL + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Card with id 999 not found"));
        }
    }
}