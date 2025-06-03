package org.example.userservice.dto.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String surname;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotNull
    @Email
    private String email;
}
