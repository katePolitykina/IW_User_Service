package org.example.userservice.dto.UserDTO;

import lombok.Data;
import org.example.userservice.dto.CardDTO.CardResponseDTO;
import org.mapstruct.Builder;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private List<CardResponseDTO> cards = new ArrayList<>();
}
