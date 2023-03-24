package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemStorage {
    Item getItemById(Long id);

    Item create(Item item);

    List<Item> getAll(User owner);

    List<Item> getAllText(String text);
}
