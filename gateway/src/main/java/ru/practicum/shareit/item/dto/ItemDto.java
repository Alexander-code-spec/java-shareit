package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.user.dto.Create;
import lombok.AllArgsConstructor;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(groups = Create.class, message = "Item name cannot be null")
    private String name;
    @NotBlank(groups = Create.class, message = "Item description cannot be null")
    private String description;
    @NotNull(groups = Create.class, message = "Available cannot be null")
    private Boolean available;
    private Long ownerId;
    private Long requestId;
}
