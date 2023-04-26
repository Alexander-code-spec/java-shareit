package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Email;

@Data
public class UserDto {
    Long id;
    @NotBlank(groups = {Create.class}, message = "Адрес электронной почты не может быть пустым.")
    String name;
    @Email(groups = {Update.class, Create.class}, message = "Название не может быть пустым.")
    @NotBlank(groups = {Create.class}, message = "Адрес электронной почты не может быть пустым.")
    String email;
}
