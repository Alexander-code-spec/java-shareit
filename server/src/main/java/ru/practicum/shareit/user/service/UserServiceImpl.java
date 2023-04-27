package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.errors.exception.ParameterException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    @Override
    @Transactional
    public UserDto save(UserDto userDto) {
        return UserMapper.toUserDto(userStorage.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
       User user = userStorage.findById(id).orElseThrow(() -> {
           throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден.");
       });

       if (userDto.getName() != null) {
           user.setName(userDto.getName());
       }
       if (userDto.getEmail() != null) {
           user.setEmail(userDto.getEmail());
       }

       try {
           return UserMapper.toUserDto(userStorage.save(user));
       } catch (DataIntegrityViolationException ex) {
           if (ex.getCause() instanceof ConstraintViolationException) {
               throw new ParameterException(ex.getMessage());
           }
       }
       return null;
    }

    @Override
    public UserDto get(Long id) {
        if (id == null) {
            throw new IncorrectParameterException("Id пользователя не может быть null");
        }
        User user = userStorage.findById(id).orElseThrow(() -> {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        });

        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IncorrectParameterException("Id не может быть пустым!");
        }
        userStorage.deleteById(id);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }
}
