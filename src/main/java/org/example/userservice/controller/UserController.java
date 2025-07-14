package org.example.userservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.UserDTO.UserRequestDTO;
import org.example.userservice.dto.UserDTO.UserResponseDTO;
import org.example.userservice.dto.UserDTO.UserUpdateDTO;
import org.example.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponseDTO getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public List<UserResponseDTO> getByIds(@RequestParam @Size(min = 1) List<@NotNull Long> ids) {
        return userService.getByIds(ids);
    }

    @GetMapping("/by-email")
    public UserResponseDTO getByEmail(@RequestParam @NotBlank String email) {
        return userService.getByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@RequestBody @Valid UserRequestDTO input, Authentication authentication) {
        return userService.create(input, authentication);

    }

    @PutMapping
    public UserResponseDTO update(@RequestBody @Valid UserUpdateDTO input, Authentication authentication) {
        return userService.update(input, authentication);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        userService.delete(id, authentication);
    }


}
