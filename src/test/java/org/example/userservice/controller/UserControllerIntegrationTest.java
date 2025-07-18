package org.example.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.example.userservice.dto.UserDTO.UserRequestDTO;
import org.example.userservice.dto.UserDTO.UserUpdateDTO;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.redis.RedisConfiguration;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "ENCRYPTION_KEY=test-key"
})
@Import(RedisConfiguration.class)
class UserControllerIntegrationTest {
    private static ObjectMapper objectMapper = new ObjectMapper()
               .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;
    private final String BASE_URL = "/api/v1.0/users";

    @Autowired
    private UserMapper userMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));

    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        Cache cache = cacheManager.getCache("userWithCards");
        if (cache != null) {
            cache.clear();
        }
    }


    @Nested
    class GetUserByIdTest {
        @Test
        void getUserById_shouldReturnUser_whenExists() throws Exception {
            var testUser = generateTestUsersToDB(1).get(0);
            mockMvc.perform(get(BASE_URL + "/{id}", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.name").value(testUser.getName()))
                    .andExpect(jsonPath("$.surname").value(testUser.getSurname()));
            var oldUsername = testUser.getName();
            testUser.setName("Hacker");
            userRepository.save(testUser);

            mockMvc.perform(get(BASE_URL + "/{id}", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(oldUsername));
        }

        @Test
        void getUserById_shouldTrow_whenNotFound() throws Exception{
            mockMvc.perform(get(BASE_URL + "/{id}", 50)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

        }
    }

    @Nested
    class GetByIdsTest {
        @Test
        @Transactional
        void getByIds_ReturnsUsers_WhenIdsAreValid() throws Exception {
            var users = generateTestUsersToDB(2);

            mockMvc.perform(get(BASE_URL)
                            .param("ids",
                                    Long.toString(users.get(0).getId()),
                                    Long.toString(users.get(1).getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(users.get(0).getId()))
                    .andExpect(jsonPath("$[1].email").value(users.get(1).getEmail()));
        }

        @Test
        @Transactional
        void getByIds_ReturnsUsers_WhenSomeIdsAreValid() throws Exception {
            var users = generateTestUsersToDB(1);

            mockMvc.perform(get(BASE_URL)
                            .param("ids",
                                    "3",
                                    Long.toString(users.get(0).getId())
                            ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(users.get(0).getId()));
        }

        @Test
        void getByIds_Returns400_WhenIdsListIsEmpty() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("ids", ""))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class GetByEmailTest {
        @Test
        void getByEmail_ReturnsUser_WhenEmailExists() throws Exception {
            var users = generateTestUsersToDB(1);
            String email = users.get(0).getEmail();

            mockMvc.perform(get(BASE_URL + "/by-email")
                            .param("email",email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(users.get(0).getId()))
                    .andExpect(jsonPath("$.email").value(email));
        }
        @Test
        void getByEmail_Returns400_WhenEmailIsBlank() throws Exception {

            mockMvc.perform(get(BASE_URL + "/by-email")
                            .param("email", ""))
                    .andExpect(status().isBadRequest());
        }
        @Test
        void getByEmail_Returns400_WhenEmailNotExists() throws Exception {

            mockMvc.perform(get(BASE_URL + "/by-email")
                            .param("email", "pypypy"))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    class CreateTests {

        @Test
        void create_Returns201_WhenInputIsValid() throws Exception {

            UserRequestDTO request = new UserRequestDTO();
            request.setName("new");
            request.setSurname("newS");
            request.setEmail("new@example.com");
            request.setBirthDate(LocalDate.now().minusYears(20));
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());
        }

        @Test
        void create_ReturnsBadRequest_WhenEmailAlreadyExists() throws Exception {
            var existing = generateTestUsersToDB(1).get(0);
            var dto = new UserRequestDTO(existing.getName(), existing.getSurname(), existing.getBirthDate(), existing.getEmail());
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("User with email " + existing.getEmail() + " already exists"));
        }

        @Test
        void create_ReturnsBadRequest_WhenInputIsInvalid() throws Exception {
            var dto = new UserRequestDTO("", "", LocalDate.now().plusYears(1), "email");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors", hasSize(4)));
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_Returns200_WhenInputIsValid() throws Exception {
            User user = generateTestUsersToDB(1).get(0);
            UserUpdateDTO dto = userMapper.toUserUpdateDTO(user);
            dto.setName("Updated User");

            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))

                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(user.getId()))
                    .andExpect(jsonPath("$.name").value("Updated User"));
        }

        @Test
        void update_ShouldEvictCache() throws Exception {

            User user = generateTestUsersToDB(1).get(0);

            mockMvc.perform(get(BASE_URL + "/" + user.getId()))
                    .andExpect(status().isOk());

            Cache cache = cacheManager.getCache("userWithCards");
            assertNotNull(cache.get(user.getId()));

            UserUpdateDTO dto = userMapper.toUserUpdateDTO(user);
            dto.setName("Updated Name");

            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            assertNull(cache.get(user.getId()));
        }


        @Test
        void update_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
            var id = 1L;
            UserUpdateDTO dto = new UserUpdateDTO(id, "Ghost", "User", LocalDate.of(1990, 1, 1), "ghost@example.com");

            mockMvc.perform(put("/api/v1.0/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User with id " + id + " not found"));
        }
        @Test
        void update_ReturnsBadRequest_WhenEmailAlreadyExists() throws Exception {
            var users = generateTestUsersToDB(2);
            UserUpdateDTO dto = userMapper.toUserUpdateDTO(users.get(0));
            dto.setEmail(users.get(1).getEmail());
            mockMvc.perform(put("/api/v1.0/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email \"" + users.get(1).getEmail() + "\" is already in use."));
        }

        @Test
        void update_ReturnsBadRequest_WhenDtoIsInvalid() throws Exception {
            UserUpdateDTO dto = new UserUpdateDTO(null, "", "", null, "not-an-email");

            mockMvc.perform(put("/api/v1.0/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors", hasSize(5)));
        }


    }

    @Nested
    class DeleteTests {
        @Test
        void delete_ReturnsOk_WhenUserExists() throws Exception {
            User savedUser = generateTestUsersToDB(1).get(0);

            mockMvc.perform(delete("/api/v1.0/users/{id}", savedUser.getId()))
                    .andExpect(status().isOk());

            assertFalse(userRepository.existsById(savedUser.getId()));
        }
        @Test
        void delete_ShouldEvictCache() throws Exception {
            User user = generateTestUsersToDB(1).get(0);

            mockMvc.perform(get(BASE_URL + "/" + user.getId()))
                    .andExpect(status().isOk());
            Cache cache = cacheManager.getCache("userWithCards");
            assertNotNull(cache.get(user.getId()));

            mockMvc.perform(delete(BASE_URL + "/" + user.getId()))
                    .andExpect(status().isOk());

            assertNull(cache.get(user.getId()));
        }

        @Test
        void delete_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
            long id = 1L;

            mockMvc.perform(delete("/api/v1.0/users/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User with id " + id + " not found"));
        }
    }

    private List<User> generateTestUsersToDB(int count) {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = new User();

            user.setEmail("user" + i +"_" + UUID.randomUUID()+ "@example.com");
            user.setName("User" + i);
            user.setSurname("TestSurname");
            user.setBirthDate(LocalDate.now().minusYears(20 + i));

            user = userRepository.save(user);
            users.add(user);
        }

        return users;
    }
}


