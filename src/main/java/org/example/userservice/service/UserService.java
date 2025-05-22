package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.userservice.dto.UserTO.UserRequestTO;
import org.example.userservice.dto.UserTO.UserResponseTO;
import org.example.userservice.dto.UserTO.UserUpdateTO;
import org.example.userservice.exception.EntityNotFoundException;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    public UserResponseTO get(Long id) {
        return userRepo
                .findById(id)
                .map(userMapper::toUserResponseTo)
                .orElseThrow(()-> new EntityNotFoundException("User with id " + id + " not found"));
    }

    public List<UserResponseTO> get(List<Long> ids) {
        return userRepo
                .getByIds(ids)
                .map(userMapper::toUserResponseTo)
                .toList();
    }

    public UserResponseTO get(String email) {
        return userRepo
                .findUserByEmail(email)
                .map(userMapper::toUserResponseTo)
                .orElseThrow(()-> new EntityNotFoundException("User with email " + email + " not found"));
    }
//TODO: add exeptions
    public UserResponseTO create(UserRequestTO input) {

        User savedUser = userRepo.save(userMapper.toUser(input));
        return userMapper.toUserResponseTo(savedUser);
    }
    @Transactional
    public void delete(Long id) {
        if (userRepo.existsById(id)) {
            userRepo.deleteById(id);
        } else {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }
    }

    public UserResponseTO update(UserUpdateTO input) {
        User user = userRepo.findById(input.getId()).orElseThrow(() -> new EntityNotFoundException("User with id " + input.getId() + " not found"));
        user.setName(input.getName());
        user.setSurname(input.getSurname());
        user.setBirthDate(input.getBirthDate());
        user.setEmail(input.getEmail());
        User updatedUser = userRepo.save(user);
        return userMapper.toUserResponseTo(updatedUser);
    }




}
