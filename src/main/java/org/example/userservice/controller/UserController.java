package org.example.userservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.UserTO.UserRequestTO;
import org.example.userservice.dto.UserTO.UserResponseTO;
import org.example.userservice.dto.UserTO.UserUpdateTO;
import org.example.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponseTO getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping("/by-ids")
    public List<UserResponseTO> getByIds(@RequestBody  @Valid @Size(min = 1) List<@NotNull Long> ids) {
        return userService.getByIds(ids);
    }

    @GetMapping("/by-email")
    public UserResponseTO getByEmail(@RequestParam @NotBlank String email) {
        return userService.getByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseTO create(@RequestBody @Valid UserRequestTO input) {
        return  userService.create(input);

    }

    @PutMapping
    public UserResponseTO update(@RequestBody @Valid UserUpdateTO input) {
        return userService.update(input);
    }

    @DeleteMapping("/{id}")
    public void delete (@PathVariable Long id) {
        userService.delete(id);
    }
}
