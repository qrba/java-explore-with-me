package ru.practicum.ewm.user.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserDto {
    private final Integer id;
    private final String email;
    private final String name;
}