package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.UserTO.UserRequestTO;
import org.example.userservice.dto.UserTO.UserResponseTO;
import org.example.userservice.dto.UserTO.UserUpdateTO;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseTO getById(Long id) {
        return userRepository
                .findById(id)
                .map(userMapper::toUserResponseTo)
                .orElseThrow(()-> new EntityNotFoundException("User with id " + id + " not found"));
    }

    public List<UserResponseTO> getByIds(List<Long> ids) {
        return userRepository
                .getByIds(ids)
                .map(userMapper::toUserResponseTo)
                .toList();
    }

    public UserResponseTO get(String email) {
        return userRepository
                .findUserByEmail(email)
                .map(userMapper::toUserResponseTo)
                .orElseThrow(()-> new EntityNotFoundException("User with email " + email + " not found"));
    }
//TODO: add exeptions
    public UserResponseTO create(UserRequestTO input) {

        User savedUser = userRepository.save(userMapper.toUser(input));
        return userMapper.toUserResponseTo(savedUser);
    }
    @Transactional
    public void delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }
    }
    @Transactional
    public UserResponseTO update(UserUpdateTO input) {
        User user = userRepository.findById(input.getId())
                .orElseThrow(() -> new EntityNotFoundException("User with id " + input.getId() + " not found"));

        userMapper.updateUserFromDto(input, user);

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseTo(updatedUser);
    }




}
