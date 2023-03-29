package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto get(Long id) {
        return ItemMapper.toItemDto(itemStorage.getItemById(id));
    }

    @Override
    public ItemDto save(ItemDto itemDto, Long userId) {
        if (userId == null) {
            throw new IncorrectParameterException("Id не задан!");
        }
        valid(itemDto);
        Optional<User> owner = Optional.ofNullable(userStorage.getUserById(userId));
        if (owner.isPresent()) {
            Item item = ItemMapper.toItem(itemDto);
            item.setOwner(owner.get());
            return ItemMapper.toItemDto(itemStorage.create(item));
        } else {
            throw new ObjectNotFoundException("Пользователь с id" + userId + "не найден");
        }
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        if (userId == null) {
            throw new IncorrectParameterException("Id пользователя не задан!");
        }

        Item item = itemStorage.getItemById(id);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Пользователь с id=" + userId + " не является владельцем вещи с id=" + id);
        }

        String patchName = itemDto.getName();
        if (Objects.nonNull(patchName) && !patchName.isEmpty()) {
            item.setName(patchName);
        }

        String patchDescription = itemDto.getDescription();
        if (Objects.nonNull(patchDescription) && !patchDescription.isEmpty()) {
            item.setDescription(patchDescription);
        }

        Boolean patchAvailable = itemDto.getAvailable();
        if (Objects.nonNull(patchAvailable)) {
            item.setAvailable(patchAvailable);
        }
        return ItemMapper.toItemDto(item);
    }


    @Override
    public List<ItemDto> getAll(Long id) {
        Optional<User> owner = Optional.ofNullable(userStorage.getUserById(id));
        if (owner.isPresent()) {
            return itemStorage.getAll(owner.get()).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        } else {
            throw new ObjectNotFoundException("Пользователь с id" + id + "не найден");
        }

    }

    @Override
    public List<ItemDto> getByText(String text) {
        if (text.isBlank()) return Collections.emptyList();
        return itemStorage.getAllText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void valid(ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new IncorrectParameterException("Не определена доступность инструмента");
        } else if (itemDto.getName() == null) {
            throw new IncorrectParameterException("Не задано название инструмента");
        } else if (itemDto.getDescription() == null) {
            throw new IncorrectParameterException("Не задано описание иснтрумента");
        } else if (itemDto.getName().isEmpty() || itemDto.getDescription().isEmpty()) {
            throw new IncorrectParameterException("Некорректно заданы поля в запросе");
        }
    }
}
