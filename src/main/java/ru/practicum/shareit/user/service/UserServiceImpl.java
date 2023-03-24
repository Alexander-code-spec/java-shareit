package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    @Override
    public UserDto save(UserDto userDto) {
        return UserMapper.toUserDto(userStorage.create(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(UserDto userDto, Long id) {
       return UserMapper.toUserDto(userStorage.update(userDto, id));
    }

    @Override
    public UserDto get(Long id) {
        return UserMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public void delete(Long id) {
        userStorage.delete(id);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }


}
