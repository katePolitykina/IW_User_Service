package org.example.userservice.dto.CardDTO;

import lombok.Data;
import java.time.LocalDate;
@Data
public class CardResponseDTO {
    private Long id;
    private String number;
    private Long userId;
    private String holder;
    private LocalDate expirationDate;
}
