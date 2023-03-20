package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface ItemService {
    ItemDto get(Integer id);

    ItemDto save(ItemDto item, Integer userId);

    ItemDto update(ItemDto item, Integer id, Integer userId);

    List<ItemDto> getAll(Integer id);

    List<ItemDto> getByText(String text);
}
