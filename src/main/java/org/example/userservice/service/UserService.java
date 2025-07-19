package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.UserDTO.UserRequestDTO;
import org.example.userservice.dto.UserDTO.UserResponseDTO;
import org.example.userservice.dto.UserDTO.UserUpdateDTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.exception.UserAlreadyExistsExeption;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Cacheable(value = "userWithCards", key = "#id")
    public UserResponseDTO getById(Long id) {
        return userRepository
                .findById(id)
                .map(userMapper::toUserResponseTo)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

    @Transactional
    public List<UserResponseDTO> getByIds(List<Long> ids) {
        return userRepository
                .getByIds(ids)
                .map(userMapper::toUserResponseTo)
                .toList();
    }

    public UserResponseDTO getByEmail(String email) {
        return userRepository
                .findUserByEmail(email)
                .map(userMapper::toUserResponseTo)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    @CacheEvict(value = "userWithCards", key = "#result.id")
    public UserResponseDTO create(UserRequestDTO input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new UserAlreadyExistsExeption("User with email " + input.getEmail() + " already exists");
        }
        User user = userMapper.toUser(input);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseTo(savedUser);
    }
    @CacheEvict(value = "userWithCards", key = "#id")
    @Transactional
    public void delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }
    }
    @CacheEvict(value = "userWithCards", key = "#input.id")
    @Transactional
    public UserResponseDTO update(UserUpdateDTO input) {
        User user = userRepository.findById(input.getId())
                .orElseThrow(() -> new EntityNotFoundException("User with id " + input.getId() + " not found"));
        if (userRepository.existsByEmailAndIdNot(input.getEmail(), input.getId())) {
            throw new BadRequestException("Email \"" + input.getEmail() + "\" is already in use.");
        }
        userMapper.updateUserFromDto(input, user);

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseTo(updatedUser);
    }


}
