package org.example.userservice.mapper;

import org.example.userservice.dto.UserDTO.PublicUserDTO;
import org.example.userservice.dto.UserDTO.UserRequestDTO;
import org.example.userservice.dto.UserDTO.UserResponseDTO;
import org.example.userservice.dto.UserDTO.UserUpdateDTO;
import org.example.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface UserMapper {
    UserResponseDTO toUserResponseTo(User user);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toUser(UserRequestDTO userRequestDTO);

    @Mapping(target = "cards", ignore = true)
    User toUser(UserUpdateDTO userUpdateDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateUserFromDto(UserUpdateDTO dto, @MappingTarget User entity);

    UserUpdateDTO toUserUpdateDTO(User user);

    PublicUserDTO toInternalUserDTO(User user);
}
