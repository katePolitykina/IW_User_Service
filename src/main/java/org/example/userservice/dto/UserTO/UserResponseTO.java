package org.example.userservice.dto.UserTO;

import lombok.Data;
import org.example.userservice.model.Card;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Data
public class UserResponseTO {

    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;

    private String email;
    private List<Card> cards = new ArrayList<>();
}
