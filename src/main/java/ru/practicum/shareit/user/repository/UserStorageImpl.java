package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.errors.exception.ParameterException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
@Slf4j
public class UserStorageImpl implements UserStorage {
    private Map<Integer, User> users = new HashMap<>();
    private Integer usersId = 0;

    @Override
    public User getUserById(Integer id) {
        return users.get(id);
    }

    @Override
    public User create(User user) {
        valid(user, false);
        this.usersId += 1;
        user.setId(usersId);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(UserDto userDto, Integer id) {
        if (!users.containsKey(id)) {
            throw new ObjectNotFoundException("Пользователь с id " + id + " не найден");
        }
        User user = users.get(id);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (checkPatchEmail(userDto.getEmail(), id).isEmpty()) {
                user.setEmail(userDto.getEmail());
            } else {
                throw new ParameterException("Пользователь с таким email уже существует");
            }
        }
        return user;
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            throw new IncorrectParameterException("Не задан id!");
        } else if (!users.containsKey(id)) {
            throw new ObjectNotFoundException("Пользователь с id " + id + " не найден");
        }
        users.remove(id);
    }

    @Override
    public List<User> getAll(){
        return new ArrayList<>(users.values());
    }

    public void valid(User user, Boolean patchFlag){
        Optional<User> obj = Optional.of(user);

        if (!obj.isPresent()) {
            throw new IncorrectParameterException("При создании пользователя передан некорреткный параметр");
        }  else if (obj.get().getEmail() == null) {
            throw new IncorrectParameterException("Email не может быть пустым");
        } else if (!isValidEmailAddress(obj.get().getEmail())) {
            throw new IncorrectParameterException("Неверно задан email");
        } else if (checkEmail(user.getEmail())) {
            throw new ParameterException("Пользователь с таким email уже существует");
        }
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "(?:[A-Za-z0-9!#$%&'*+/=?.^_`{|}~]" +
                "+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~]+)*|" +
                "\\\"(?:[x01-x08x0bx0cx0e-x1fx21x23-x5bx5d-x7f]|[x01-x09x0bx0cx0e-x7f])*\\\")" +
                "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)" +
                "+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|" +
                "[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])).){3}(?:(2(5[0-5]|" +
                "[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[x01-x08x0bx0cx0e-x1fx21-x5ax53-x7f]" +
                "|[x01-x09x0bx0cx0e-x7f])+)])";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public Optional<User> checkPatchEmail(String email, Integer id){
        return users.values().stream()
                .filter(user -> Objects.equals(user.getEmail(), email) && !Objects.equals(user.getId(), id))
                .findAny();
    }

    public boolean checkEmail(String email){
        for (User user: users.values()) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
}
