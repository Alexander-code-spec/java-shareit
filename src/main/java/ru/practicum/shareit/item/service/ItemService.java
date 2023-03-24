package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto get(Long id);

    ItemDto save(ItemDto item, Long userId);

    ItemDto update(ItemDto item, Long id, Long userId);

    List<ItemDto> getAll(Long id);

    List<ItemDto> getByText(String text);
}
