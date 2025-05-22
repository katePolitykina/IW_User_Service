package org.example.userservice.dto.UserTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.example.userservice.model.Card;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Data
public class UserRequestTO {
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
