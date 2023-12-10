package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.model.dto.NewUserRequest;
import ru.practicum.ewm.user.model.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size);

    UserDto addUser(NewUserRequest newUserRequest);

    void deleteUser(int userId);
}