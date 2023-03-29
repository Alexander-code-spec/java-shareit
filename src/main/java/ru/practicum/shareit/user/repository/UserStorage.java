package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User getUserById(Long id);

    User create(User user);

    User update(UserDto user, Long id);

    void delete(Long id);

    List<User> getAll();
}
