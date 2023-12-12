package ru.practicum.ewm.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.exception.UserAlreadyExistsException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.dto.NewUserRequest;
import ru.practicum.ewm.user.model.dto.UserDto;
import ru.practicum.ewm.user.service.UserServiceImpl;
import ru.practicum.ewm.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static ru.practicum.ewm.user.model.dto.UserMapper.userFromNewUserRequest;
import static ru.practicum.ewm.user.model.dto.UserMapper.userToUserDto;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private UserServiceImpl userService;

    private final NewUserRequest newUserRequest = new NewUserRequest("user@email.com", "name");

    @Test
    public void shouldAddUser() {
        Mockito
                .when(userStorage.save(any(User.class)))
                .then(returnsFirstArg());

        UserDto userDto = userToUserDto(
                userStorage.save(
                        userFromNewUserRequest(
                                newUserRequest
                        )
                )
        );

        assertThat(userDto.getName(), equalTo(newUserRequest.getName()));
        assertThat(userDto.getEmail(), equalTo(newUserRequest.getEmail()));
    }

    @Test
    public void shouldNotAddUserWhenEmailNotUnique() {
        Mockito
                .when(userStorage.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        UserAlreadyExistsException e = Assertions.assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.addUser(newUserRequest)
        );

        assertThat(e.getMessage(), equalTo("Пользователь с email 'user@email.com' уже существует"));
    }

    @Test
    public void shouldDeleteUser() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        userService.deleteUser(1);

        Mockito.verify(userStorage).deleteById(anyInt());
    }

    @Test
    public void shouldNotDeleteUserWhenUserNotFound() {
        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(1)
        );

        assertThat(e.getMessage(), equalTo("Пользователь с id=1 не найден"));
    }

    @Test
    public void shouldGetUsers() {
        User user = new User(1, "user@email.com", "name");
        Mockito
                .when(userStorage.findUsers(anyList(), any(Pageable.class)))
                .thenReturn(List.of(user));
        List<UserDto> userDtoList = userService.getUsers(Collections.emptyList(), 0, 10);

        assertThat(userDtoList.size(), equalTo(1));

        UserDto userDto = userDtoList.get(0);

        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(userDto.getEmail(), equalTo(userDto.getEmail()));
    }
}