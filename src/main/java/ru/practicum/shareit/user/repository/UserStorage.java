package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User getUserById(Integer id);
    User create(User user);
    User update(UserDto user, Integer id);
    void delete(Integer id);
    List<User> getAll();
}
