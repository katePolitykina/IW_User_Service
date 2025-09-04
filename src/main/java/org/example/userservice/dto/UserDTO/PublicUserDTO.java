package org.example.userservice.dto.UserDTO;

import lombok.Data;

@Data
public class PublicUserDTO {
    private Long id;
    private String name;
    private String surname;
    private String email;
}