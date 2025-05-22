package org.example.userservice.dto.CardTO;

import lombok.Data;

import java.time.LocalDate;
@Data
public class CardResponseTO {
    private Long id;
    private String number;
    private Long userId;
    private String holder;
    private LocalDate expirationDate;
}
