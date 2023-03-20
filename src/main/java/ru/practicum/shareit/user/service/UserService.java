package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto save(UserDto user);

    UserDto update(UserDto user, Integer id);

    UserDto get(Integer id);

    void delete(Integer id);

    List<UserDto> getAll();
}
