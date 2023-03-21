package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemStorage {
    Item getItemById(Integer id);

    Item create(Item user);

    List<Item> getAll(User owner);

    List<Item> getAllText(String text);
}
