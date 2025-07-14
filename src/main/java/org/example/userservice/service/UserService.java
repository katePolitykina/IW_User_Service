package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.UserDTO.UserRequestDTO;
import org.example.userservice.dto.UserDTO.UserResponseDTO;
import org.example.userservice.dto.UserDTO.UserUpdateDTO;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.exception.UserAlreadyExistsException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    //TODO: Do I need to implement additional security for getByID/getByIds
    // or is given card information fine for showing
    @Cacheable(value = "userWithCards", key = "#id")
    public UserResponseDTO getById(Long id) {
        return userRepository
                .findById(id)
                .map(userMapper::toUserResponseTo)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

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
    public UserResponseDTO create(UserRequestDTO input, Authentication authentication) {
        if (!authentication.getName().equals(input.getEmail())) {
            throw new AccessDeniedException("Access denied: You can only create your own user account.");
        }
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + input.getEmail() + " already exists");
        }
        User user = userMapper.toUser(input);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseTo(savedUser);
    }
    @CacheEvict(value = "userWithCards", key = "#id")
    @Transactional
    public void delete(Long id, Authentication authentication) {
            User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
            if (!authentication.getName().equals(user.getEmail())) {
                throw new AccessDeniedException("Access denied: You can only delete your own user account.");
            }
            userRepository.deleteById(id);

    }
    @CacheEvict(value = "userWithCards", key = "#input.id")
    @Transactional
    public UserResponseDTO update(UserUpdateDTO input, Authentication authentication) {
        if (!authentication.getName().equals(input.getEmail())) {
            throw new AccessDeniedException("Access denied: You can only update your own user account.");
        }

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
