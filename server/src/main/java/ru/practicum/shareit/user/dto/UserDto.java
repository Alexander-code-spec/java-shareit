package ru.practicum.shareit.user.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    Long id;
    String name;
    String email;
}
