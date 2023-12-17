package ru.practicum.ewm.user.model.dto;

import ru.practicum.ewm.user.model.User;

public class UserMapper {
    public static User userFromNewUserRequest(NewUserRequest newUserRequest) {
        return new User(
                null,
                newUserRequest.getEmail(),
                newUserRequest.getName()
        );
    }

    public static UserDto userToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public static UserShortDto userToShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }
}