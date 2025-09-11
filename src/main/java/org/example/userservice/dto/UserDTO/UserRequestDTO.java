package org.example.userservice.dto.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {
    @NotNull @NotEmpty
    private String keycloakId;
    @NotNull @NotEmpty
    private String name;
    @NotNull @NotEmpty
    private String surname;
    @NotNull
    @Past
    private LocalDate birthDate;
    @NotNull
    @Email
    private String email;
}
