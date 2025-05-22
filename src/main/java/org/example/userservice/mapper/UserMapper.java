package org.example.userservice.mapper;
import org.example.userservice.dto.UserTO.UserRequestTO;
import org.example.userservice.dto.UserTO.UserResponseTO;
import org.example.userservice.dto.UserTO.UserUpdateTO;
import org.example.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper  {
    UserResponseTO toUserResponseTo(User user);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toUser(UserRequestTO userRequestTo);

    @Mapping(target = "cards", ignore = true)
    User toUser(UserUpdateTO userUpdateTO);
}
