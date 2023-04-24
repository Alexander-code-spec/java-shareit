package ru.practicum.shareit.request.service;

import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;

import java.util.List;

import static ru.practicum.shareit.request.mapper.ItemRequestMapper.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static java.time.LocalDateTime.*;
import static org.mockito.Mockito.*;
import static java.util.List.*;


@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    @Mock
    private ItemRequestStorage itemRequestRepository;
    ItemRequestService itemRequestService;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    @Mock
    private ItemService itemService;
    @Mock
    private UserService userService;
    private UserDto userDto;

    @BeforeEach
    void initialize() {
        userDto = new UserDto(
                1L,
                "marry",
                "marry@mail.com");
        itemRequestDto = new ItemRequestDto(
                1L,
                "my request",
                1L,
                now(),
                of()
        );
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, userService, itemService);
        itemRequest = mapToItemRequest(itemRequestDto, userDto);
    }

    ItemRequestDto saveItemRequestDto() {
        when(userService.get(any()))
                .thenReturn(userDto);
        when(itemRequestRepository.save(any()))
                .thenReturn(itemRequest);
        return itemRequestService.save(itemRequestDto, userDto.getId());
    }

    @Test
    void saveItemEmptyDescriptionTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemRequestService.save(
                        new ItemRequestDto(
                                1L,
                                "",
                                1L,
                                now(),
                                of()
                        ),
                        1L)
        );
        assertEquals("Запрос не может быть null или пустым", exception.getMessage());
    }

    @Test
    void saveItemRequestTest() {
        ItemRequestDto dto = saveItemRequestDto();
        assertEquals(dto.getRequesterId(), itemRequest.getRequester().getId());
        assertEquals(dto.getDescription(), itemRequest.getDescription());
        assertEquals(dto.getId(), itemRequest.getId());
    }

    @Test
    void getItemRequestsTest() {
        saveItemRequestDto();
        when(itemRequestRepository.findItemRequestByRequesterOrderByCreatedDesc(any()))
                .thenReturn(of(itemRequest));
        List<ItemRequestDto> allItemRequests = itemRequestService.getAllItemRequests(userDto.getId());
        assertEquals(allItemRequests.get(0).getId(), itemRequest.getId());
        assertEquals(allItemRequests.get(0).getItems().size(), 0);
        assertEquals(allItemRequests.size(), 1);
    }

    @Test
    void getItemRequestsItemsTest() {
        saveItemRequestDto();
        when(itemService.getItemsByRequests(any()))
                .thenReturn(of(
                                new ItemDto(
                                        1L,
                                        "Toy",
                                        "my toy",
                                        true,
                                        3L)
                        )
                );
        when(itemRequestRepository.findItemRequestByRequesterOrderByCreatedDesc(any()))
                .thenReturn(of(itemRequest));
        List<ItemRequestDto> allItemRequests = itemRequestService.getAllItemRequests(userDto.getId());
        assertEquals(allItemRequests.get(0).getId(), itemRequest.getId());
        assertEquals(allItemRequests.size(), 1);
    }

    @Test
    void getItemRequestTest() {
        saveItemRequestDto();
        when(itemService.getItemsByRequestId(any()))
                .thenReturn(of(
                                new ItemDto(
                                        1L,
                                        "toy",
                                        "my toy",
                                        true,
                                        3L)
                        )
                );
        when(itemRequestRepository.findById(any()))
                .thenReturn(java.util.Optional.ofNullable(itemRequest));
        ItemRequestDto itemRequestById = itemRequestService.getItemRequestById(2, userDto.getId());
        assertEquals(itemRequestById.getDescription(), itemRequest.getDescription());
        assertEquals(itemRequestById.getId(), itemRequest.getId());
        assertEquals(itemRequestById.getItems().size(), 1);
    }

    @Test
    void getItemRequestsEmptyTest() {
        saveItemRequestDto();
        when(itemRequestRepository.findItemRequestByRequesterOrderByCreatedDesc(any()))
                .thenReturn(of());
        List<ItemRequestDto> allItemRequests = itemRequestService.getAllItemRequests(userDto.getId());
        assertEquals(allItemRequests.size(), 0);
    }

    @Test
    void getAllItemRequestsTest() {
        saveItemRequestDto();
        when(itemService.getItemsByRequests(any()))
                .thenReturn(of(
                                new ItemDto(
                                        1L,
                                        "toy",
                                        "my toy",
                                        true,
                                        3L)
                        )
                );
        when(itemRequestRepository.findItemRequestByRequester_IdIsNotOrderByCreatedDesc(any()))
                .thenReturn(of(itemRequest));
        List<ItemRequestDto> allItemRequests = itemRequestService.getAllItemRequests(null, null, userDto.getId());
        assertEquals(allItemRequests.get(0).getId(), itemRequest.getId());
        assertEquals(allItemRequests.get(0).getItems().size(), 1);
        assertEquals(allItemRequests.size(), 1);
    }

    @Test
    void getItemRequestNotFoundTest() {
        when(userService.get(any()))
                .thenReturn(userDto);
        when(itemRequestRepository.findById(any()))
                .thenThrow(ObjectNotFoundException.class);
        when(itemService.getItemsByRequestId(any()))
                .thenReturn(of());
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getItemRequestById(42L, 42L));
    }
}