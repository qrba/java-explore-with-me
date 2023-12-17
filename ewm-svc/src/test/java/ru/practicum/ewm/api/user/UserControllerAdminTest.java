package ru.practicum.ewm.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.user.controller.UserControllerAdmin;
import ru.practicum.ewm.user.model.dto.NewUserRequest;
import ru.practicum.ewm.user.model.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserControllerAdmin.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerAdminTest {
    @MockBean
    private UserService userService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private final UserDto userDto = new UserDto(1, "user@email.com", "username");

    @Test
    public void shouldAddUser() throws Exception {
        NewUserRequest newUser = new NewUserRequest("user@email.com", "username");
        Mockito
                .when(userService.addUser(any(NewUserRequest.class)))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.name").value(userDto.getName()));
    }

    @Test
    public void shouldNotAddUserWhenNameBlank() throws Exception {
        NewUserRequest newUser = new NewUserRequest("user@email.com", "");

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddUserWhenNameTooShort() throws Exception {
        NewUserRequest newUser = new NewUserRequest("user@email.com", "n");

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddUserWhenNameTooLong() throws Exception {
        NewUserRequest newUser = new NewUserRequest(
                "user@email.com",
                "Very very very very very very very very very very very very very very very very very very " +
                        "very very very very very very very very very very very very very very very very very very " +
                        "very very very very very very very very very very very very very long name"
        );

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddUserWhenEmailIncorrect() throws Exception {
        NewUserRequest newUser = new NewUserRequest("user", "name");

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddUserWhenEmailNull() throws Exception {
        NewUserRequest newUser = new NewUserRequest(null, "name");

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddUserWhenEmailTooShort() throws Exception {
        NewUserRequest newUser = new NewUserRequest("e@m.c", "name");

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddUserWhenEmailTooLong() throws Exception {
        NewUserRequest newUser = new NewUserRequest(
                "Veryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryvery" +
                        "veryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryveryvery" +
                        "veryveryveryveryveryveryveryveryveryveryveryveryveryveryveryverylong@mail.com",
                "name"
        );

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        mvc.perform(delete("/admin/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Mockito.verify(userService).deleteUser(anyInt());
    }

    @Test
    public void shouldGetUsers() throws Exception {
        Mockito
                .when(userService.getUsers(any(), anyInt(), anyInt()))
                .thenReturn(List.of(userDto));

        mvc.perform(get("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$.[0].email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.[0].name").value(userDto.getName()));
    }
}