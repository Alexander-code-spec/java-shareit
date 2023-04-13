package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserShortDto {
    Long id;
    @NotEmpty(groups = {Create.class}, message = "Адрес электронной почты не может быть пустым.")
    String name;
}
