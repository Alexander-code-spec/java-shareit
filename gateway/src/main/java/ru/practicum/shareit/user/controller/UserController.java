package ru.practicum.shareit.user.controller;

import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.user.dto.Create;
import ru.practicum.shareit.user.dto.Update;
import ru.practicum.shareit.user.client.UserClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserClient userClient;

    @Validated(Create.class)
    @PostMapping()
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) {
        valid(userDto);
        return userClient.createUser(userDto);
    }

    @Validated(Update.class)
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserDto userDto,
                                             @PathVariable Long userId) {
        return userClient.updateUser(userDto, userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@NotNull @PathVariable Long userId) {
        return userClient.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userClient.deleteUser(userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllUsers() {
        return userClient.getUsers();
    }

    public void valid(UserDto user) {
        if (user == null) {
            throw new IncorrectParameterException("При создании пользователя передан некорреткный параметр");
        }  else if (user.getEmail() == null) {
            throw new IncorrectParameterException("Email не может быть пустым");
        } else if (!isValidEmailAddress(user.getEmail())) {
            throw new IncorrectParameterException("Неверно задан email");
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
}
