package ru.practicum.shareit.user.service;

import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.user.mapper.UserMapper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static java.util.Optional.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserStorage userRepository;
    private UserService userService;
    private UserDto userDto;
    private User user;

    @BeforeEach
    void initialize() {
        userService = new UserServiceImpl(userRepository);
        userDto = UserDto.builder()
                .id(1L)
                .name("Paul")
                .email("paul@mail.com")
                .build();
        user = toUser(userDto);
    }

    @Test
    void saveUserNullEmailTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> userService.save(new UserDto(
                        null,
                        "Molly",
                        null))
        );
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    @Test
    void saveTest() {
        when(userRepository.save(any()))
                .thenReturn(user);
        UserDto save = userService.save(userDto);
        assertEquals(save.getEmail(), user.getEmail());
        assertEquals(save.getName(), user.getName());
        assertEquals(save.getId(), user.getId());
    }

    @Test
    void saveUserSameEmailTest() {
        when(userRepository.save(any()))
                .thenThrow(IncorrectParameterException.class);
        assertThrows(IncorrectParameterException.class,
                () -> userService.save(userDto));

    }

    @Test
    void saveUserEmptyEmailTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> userService.save(new UserDto(
                        null, "Abbie", "mail"))
        );
        assertEquals("Неверно задан email", exception.getMessage());
    }

    @Test
    void saveNullUserEmptyTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> userService.save(null)
        );
        assertEquals("При создании пользователя передан некорреткный параметр", exception.getMessage());
    }

    @Test
    void saveNullEmailTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> userService.save(new UserDto(
                        null, "Abbie", null))
        );
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    @Test
    void updateUserNameTest() {
        UserDto userDto1 = new UserDto(1L, "Daniel", null);
        UserDto userDto2 = new UserDto(1L, "Daniel", userDto.getEmail());
        when(userRepository.save(any()))
                .thenReturn(user);
        userService.save(userDto);
        when(userRepository.save(any()))
                .thenReturn(toUser(userDto2));
        when(userRepository.findById(any()))
                .thenReturn(ofNullable(toUser(userDto2)));
        UserDto dto = userService.update(userDto1, userDto2.getId());
        assertNotEquals(dto.getEmail(), userDto1.getEmail());
        assertEquals(dto.getName(), userDto2.getName());
        assertEquals(dto.getId(), userDto2.getId());
    }

    @Test
    void updateTest() {
        var updatedUser = new UserDto(
                1L,
                "Nagel",
                "nagel@mail.com");
        when(userRepository.save(any()))
                .thenReturn(user);
        userService.save(userDto);
        when(userRepository.save(any()))
                .thenReturn(toUser(updatedUser));
        when(userRepository.findById(any()))
                .thenReturn(ofNullable(toUser(updatedUser)));
        UserDto dto = userService.update(updatedUser, updatedUser.getId());
        assertEquals(dto.getEmail(), updatedUser.getEmail());
        assertEquals(dto.getName(), updatedUser.getName());
        assertEquals(dto.getId(), updatedUser.getId());
    }

    @Test
    void getUserUserNotFoundTest() {
        when(userRepository.findById(any()))
                .thenThrow(ObjectNotFoundException.class);
        assertThrows(ObjectNotFoundException.class,
                () -> userService.get(7L));
    }

    @Test
    void updateUserEmailTest() {
        UserDto userDto1 = new UserDto(1L, null, "john@mail.com");
        UserDto userDto2 = new UserDto(1L, userDto.getName(), "john@mail.com");
        when(userRepository.save(any()))
                .thenReturn(user);
        userService.save(userDto);
        when(userRepository.save(any()))
                .thenReturn(toUser(userDto2));
        when(userRepository.findById(any()))
                .thenReturn(ofNullable(toUser(userDto2)));
        UserDto dto = userService.update(userDto1, userDto2.getId());
        assertNotEquals(dto.getName(), userDto1.getName());
        assertEquals(dto.getEmail(), userDto2.getEmail());
        assertEquals(dto.getId(), userDto2.getId());
    }

    @Test
    void updateUserSameEmailTest() {
        UserDto dto = new UserDto(2L, "Paul", "paul@mail.com");
        when(userRepository.findById(any()))
                .thenReturn(ofNullable(toUser(dto)));
        when(userRepository.save(any()))
                .thenThrow(IncorrectParameterException.class);
        assertThrows(IncorrectParameterException.class,
                () -> userService.update(
                        userDto,
                        1L)
        );
    }

    @Test
    void deleteTest() {
        when(userRepository.save(any()))
                .thenReturn(user);
        UserDto dto = userService.save(userDto);
        userService.delete(dto.getId());
        verify(userRepository, times(1))
                .deleteById(user.getId());
    }

    @Test
    void getUserNullTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> userService.get(null));
        assertEquals("Id пользователя не может быть null", exception.getMessage());
    }

    @Test
    void deleteUserNullIdTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> userService.delete(null));
        assertEquals("Id не может быть пустым!", exception.getMessage());
    }

    @Test
    void getAllEmptyTest() {
        when(userRepository.findAll())
                .thenReturn(List.of());
        List<UserDto> dtos = userService.getAll();
        assertEquals(dtos.size(), 0);
    }

    @Test
    void getAllTest() {
        when(userRepository.findAll())
                .thenReturn(List.of(user));
        List<UserDto> dtos = userService.getAll();
        assertEquals(dtos.get(0).getId(), user.getId());
        assertEquals(dtos.size(), 1);
    }
}
