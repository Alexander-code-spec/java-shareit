package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemService {
    ItemAllDto get(Long id, Long userId);

    ItemDto save(ItemDto item, ItemRequestDto itemRequestDto, Long userId);

    ItemDto update(ItemDto item, Long id, Long userId);

    List<ItemAllDto> getAll(Long id, Integer from, Integer size);

    List<ItemDto> getByText(String text, Long userId, Integer from, Integer size);

    CommentDto createComment(CommentDto comment, Long itemId, Long userId);

    List<CommentDto> getAllComments();

    List<ItemDto> getItemsByRequests(List<ItemRequest> requests);

    List<ItemDto> getItemsByRequestId(Long requestId);
}
