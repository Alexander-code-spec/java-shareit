package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.util.Pagination.makePageRequest;

@Slf4j
@Service
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService{
    private final ItemRequestStorage itemRequestStorage;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestStorage itemRequestStorage, UserService userService, ItemService itemService) {
        this.itemRequestStorage = itemRequestStorage;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Integer from, Integer size, Long userId) {
        List<ItemRequest> requests;
        PageRequest pageRequest = makePageRequest(from, size, Sort.by("created").descending());
        if (pageRequest == null) {
            requests = itemRequestStorage.findItemRequestByRequester_IdIsNotOrderByCreatedDesc(userId);
        } else {
            requests = itemRequestStorage.findItemRequestByRequester_IdIsNotOrderByCreatedDesc(userId, pageRequest)
                    .stream()
                    .collect(toList());
        }
        List<ItemDto> items = itemService.getItemsByRequests(requests);
        return requests
                .stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDto(itemRequest, items))
                .collect(toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId) {
        User user = UserMapper.toUser(userService.get(userId));
        List<ItemRequest> itemRequests = itemRequestStorage.findItemRequestByRequesterOrderByCreatedDesc(user);
        List<ItemDto> items = itemService.getItemsByRequests(itemRequests);
        return itemRequests.stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDto(itemRequest, items))
                .collect(toList());
    }

    @Override
    public ItemRequestDto save(ItemRequestDto itemRequestDto, Long requesterId) {
        valid(itemRequestDto);
        User user = UserMapper.toUser(userService.get(requesterId));
        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequest(itemRequestDto);
        itemRequest.setRequester(user);
        itemRequest.setCreated(now());
        ItemRequest requestForSave = itemRequestStorage.save(itemRequest);
        return ItemRequestMapper.mapToItemRequestDto(requestForSave);
    }

    @Override
    public ItemRequestDto getItemRequestById(long requestId, Long userId) {
        User owner = UserMapper.toUser(userService.get(userId));
        if (owner != null) {
            List<ItemDto> items = itemService.getItemsByRequestId(requestId);
            ItemRequest itemRequest = itemRequestStorage.findById(requestId).orElseThrow(
                    () -> new ObjectNotFoundException("Запрос с id = " + requestId + " не найден"));
            return ItemRequestMapper.mapToItemRequestDto(itemRequest, items);
        } else {
            throw new ObjectNotFoundException("Пользователь с id" + userId + "не найден");
        }
    }

    private void valid(ItemRequestDto itemRequestDto) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new IncorrectParameterException("Запрос не может быть null или пустым");
        }
    }
}
