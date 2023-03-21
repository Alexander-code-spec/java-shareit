package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemStorageImpl implements ItemStorage {
    private Map<Integer, Item> items = new HashMap<>();
    private Integer itemsId = 0;

    @Override
    public Item getItemById(Integer id) {
        if(id == null) {
            throw new IncorrectParameterException("Не задан id!");
        } else if(!items.containsKey(id)) {
            throw new ObjectNotFoundException("Вещь с id " + id + " не найден");
        }
        return items.get(id);
    }

    @Override
    public Item create(Item item) {
        this.itemsId += 1;
        item.setId(itemsId);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getAll(User owner) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwner(), owner))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getAllText(String text) {
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text) && item.isAvailable())
                .collect(Collectors.toList());
    }
}
