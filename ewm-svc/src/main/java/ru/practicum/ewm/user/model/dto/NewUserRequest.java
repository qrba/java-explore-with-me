package ru.practicum.ewm.user.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@RequiredArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Email пользователя не может быть пустым")
    @Email(message = "Email пользователя должен быть корректным")
    @Size(min = 6, max = 254, message = "Размер email пользователя должен быть между 6 и 254 символами")
    private final String email;
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 250, message = "Размер имени пользователя должен быть между 2 и 250 символами")
    private final String name;
}