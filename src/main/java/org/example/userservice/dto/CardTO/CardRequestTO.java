package org.example.userservice.dto.CardTO;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
public class CardRequestTO {
    @NotNull
    @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
    private String number;

    @NotNull
    private Long userId;

    @Size(min = 2, max = 255, message = "Card holder name must be between 2 and 255 characters")
    private String holder;

    @NotNull
    @Future
    private LocalDate expirationDate;

}
