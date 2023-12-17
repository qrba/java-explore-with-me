package ru.practicum.ewm.user.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserShortDto {
    private final Integer id;
    private final String name;
}