package ru.practicum.ewm.user.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@RequiredArgsConstructor
public class NewUserRequest {
    private final Integer id;
    @NotBlank(message = "Email пользователя не может быть пустым")
    @Email(message = "Email пользователя должен быть корректным")
    private final String email;
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private final String name;
}