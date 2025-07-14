package org.example.userservice.service;

import org.example.userservice.dto.UserDTO.UserRequestDTO;
import org.example.userservice.dto.UserDTO.UserResponseDTO;
import org.example.userservice.dto.UserDTO.UserUpdateDTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.exception.UserAlreadyExistsException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getById_shouldReturnUser_whenExists() {
        User user = new User();
        user.setId(1L);
        UserResponseDTO dto = new UserResponseDTO();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseTo(user)).thenReturn(dto);

        UserResponseDTO result = userService.getById(1L);

        assertEquals(dto, result);
    }

    @Test
    void getById_shouldThrow_whenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void getByIds_shouldReturnList() {
        User user1 = new User();
        User user2 = new User();
        UserResponseDTO dto1 = new UserResponseDTO();
        UserResponseDTO dto2 = new UserResponseDTO();
        when(userRepository.getByIds(List.of(1L, 2L))).thenReturn(Stream.of(user1, user2));
        when(userMapper.toUserResponseTo(user1)).thenReturn(dto1);
        when(userMapper.toUserResponseTo(user2)).thenReturn(dto2);

        List<UserResponseDTO> result = userService.getByIds(List.of(1L, 2L));

        assertEquals(List.of(dto1, dto2), result);
    }

    @Test
    void getByIds_shouldReturnEmptyList_whenNoUsers() {
        when(userRepository.getByIds(List.of(1L, 2L))).thenReturn(Stream.empty());

        List<UserResponseDTO> result = userService.getByIds(List.of(1L, 2L));
        assertTrue(result.isEmpty());
    }

    @Test
    void getByEmail_shouldReturnUser_whenExists() {
        User user = new User();
        UserResponseDTO dto = new UserResponseDTO();
        when(userRepository.findUserByEmail("a@mail.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseTo(user)).thenReturn(dto);

        UserResponseDTO result = userService.getByEmail("a@mail.com");
        assertEquals(dto, result);
    }

    @Test
    void getByEmail_shouldThrow_whenNotExists() {
        when(userRepository.findUserByEmail("a@mail.com")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getByEmail("a@mail.com"));
    }

    @Test
    void create_shouldThrow_whenEmailExists() {
        UserRequestDTO req = new UserRequestDTO();
        req.setEmail("test@mail.com");
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.create(req));
    }

    @Test
    void create_shouldReturnUser_whenOk() {
        UserRequestDTO req = new UserRequestDTO();
        req.setEmail("test@mail.com");
        User user = new User();
        User saved = new User();
        UserResponseDTO dto = new UserResponseDTO();
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userMapper.toUser(req)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toUserResponseTo(saved)).thenReturn(dto);

        UserResponseDTO result = userService.create(req);

        assertEquals(dto, result);
    }

    @Test
    void delete_shouldThrow_whenUserNotExists() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> userService.delete(1L));
    }

    @Test
    void delete_shouldRun_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void update_shouldThrow_whenUserNotFound() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.update(dto));
    }

    @Test
    void update_shouldThrow_whenEmailBusy() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        dto.setEmail("test@mail.com");
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("test@mail.com", 1L)).thenReturn(true);
        assertThrows(BadRequestException.class, () -> userService.update(dto));
    }

    @Test
    void update_shouldUpdate_whenOk() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        dto.setEmail("test@mail.com");
        User user = new User();
        User updated = new User();
        UserResponseDTO response = new UserResponseDTO();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("test@mail.com", 1L)).thenReturn(false);
        doNothing().when(userMapper).updateUserFromDto(dto, user);
        when(userRepository.save(user)).thenReturn(updated);
        when(userMapper.toUserResponseTo(updated)).thenReturn(response);

        UserResponseDTO result = userService.update(dto);

        assertEquals(response, result);
    }
}