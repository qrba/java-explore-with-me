package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.UserAlreadyExistsException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.dto.NewUserRequest;
import ru.practicum.ewm.user.model.dto.UserDto;
import ru.practicum.ewm.user.model.dto.UserMapper;
import ru.practicum.ewm.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.user.model.dto.UserMapper.userFromNewUserRequest;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size) {
        log.info("Запрошен список пользователей");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return userRepository.findUsers(ids, pageable).stream()
                .map(UserMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto addUser(NewUserRequest newUserRequest) {
        try {
            User user = userRepository.save(userFromNewUserRequest(newUserRequest));
            log.info("Добавлен пользователь {}", user);
            return UserMapper.userToUserDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(
                    "Пользователь с email '" + newUserRequest.getEmail() + "' уже существует"
            );
        }
    }

    @Override
    public void deleteUser(int userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден");
        userRepository.deleteById(userId);
        log.info("Удален пользователь с id={}", userId);
    }
}