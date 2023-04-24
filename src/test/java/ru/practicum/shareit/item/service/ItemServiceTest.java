package ru.practicum.shareit.item.service;

import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentStorage;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.service.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.when;
import static java.util.List.*;
import static ru.practicum.shareit.user.mapper.UserMapper.toUser;


@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private CommentStorage commentRepository;
    @Mock
    private BookingService bookingService;
    @Mock
    private ItemStorage itemRepository;
    @Mock
    private UserService userService;
    private ItemService itemService;
    private ItemDto itemDto;
    private UserDto userDto;
    private Item item;

    @BeforeEach
    void initialize() {
        itemService = new ItemServiceImpl(
                itemRepository,
                userService,
                commentRepository,
                bookingService
        );
        userDto = new UserDto(
                1L,
                "Eddie",
                "eddie@mail.com");
        item = new Item(
                1L,
                "Pocket",
                "Deep pocket",
                true,
                toUser(userDto),
                null);
        itemDto = ItemMapper.toItemDto(item);
    }

    private ItemDto saveItemDto() {
        when(userService.get(any()))
                .thenReturn(userDto);
        when(itemRepository.save(any()))
                .thenReturn(ItemMapper.toItem(itemDto));
        return itemService.save(itemDto, null, userDto.getId());
    }

    @Test
    void saveTest() {
        ItemDto saved = saveItemDto();
        assertEquals(saved.getName(), item.getName());
        assertEquals(saved.getId(), item.getId());
    }

    @Test
    void updateTest() {
        ItemDto dto = saveItemDto();
        Item updated = new Item(
                dto.getId(),
                "Anthony",
                itemDto.getDescription(),
                itemDto.getAvailable(),
                toUser(userDto),
                null
        );
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any()))
                .thenReturn(updated);
        ItemDto update = itemService.update(ItemMapper.toItemDto(updated), userDto.getId(), 1L);
        assertNotEquals(dto.getName(), update.getName());
        assertEquals(1L, update.getId());
    }

    @Test
    void searchTest() {
        saveItemDto();
        when(itemRepository.getAllText(anyString()))
                .thenReturn(of(item));
        List<ItemDto> search = itemService.getByText(
                "oops",
                userDto.getId(),
                null,
                null
        );
        assertEquals(search.get(0).getId(), item.getId());
        assertEquals(search.size(), 1);
    }

    @Test
    void updateNullOwnerTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.update(itemDto, null, null));
        assertEquals("Id пользователя не задан!", exception.getMessage());
    }

    @Test
    void getItemNotFoundTest() {
        saveItemDto();
        when(itemRepository.findById(anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.get(42L, userDto.getId()));
    }

    @Test
    void saveCommentNotFoundItemTest() {
        var commentDto = new CommentDto(
                1L,
                "pink rose",
                itemDto.getId(),
                userDto.getName(),
                now()
        );
        when(itemRepository.findById(anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.createComment(commentDto, 42L, 2L));
    }

    @Test
    void searchEmptyTextTest() {
        List<ItemDto> search = itemService.getByText(
                "",
                userDto.getId(),
                null,
                null
        );
        assertEquals(search.size(), 0);
    }

    @Test
    void searchEmptyResultTest() {
        saveItemDto();
        when(itemRepository.getAllText(anyString()))
                .thenReturn(of());
        List<ItemDto> search = itemService.getByText(
                "Golden hand",
                userDto.getId(),
                null,
                null
        );
        assertEquals(search.size(), 0);
    }

    @Test
    void searchNullTextTest() {
        var commentDto = new CommentDto(
                1L,
                null,
                itemDto.getId(),
                userDto.getName(),
                now()
        );
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.createComment(commentDto, itemDto.getId(), 2L));
        assertEquals("Текст комментария не может быть пустым", exception.getMessage());
    }

    @Test
    void saveCommentEmptyTextTest() {
        var commentDto = new CommentDto(
                1L,
                "",
                itemDto.getId(),
                userDto.getName(),
                now()
        );
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.createComment(commentDto, itemDto.getId(), 2L));
        assertEquals("Текст комментария не может быть пустым", exception.getMessage());
    }

    @Test
    void saveItemEmptyNameTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                "",
                                "Blue pen",
                                true,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Некорректно заданы поля в запросе", exception.getMessage());
    }

    @Test
    void getAllCommentsTest() {
        var commentDto = new CommentDto(
                1L,
                "space",
                itemDto.getId(),
                userDto.getName(),
                now()
        );
        var comment = new Comment(
                1L,
                commentDto.getText(),
                item,
                toUser(userDto),
                now()
        );
        when(commentRepository.findAll())
                .thenReturn(of(comment));
        List<CommentDto> allComments = itemService.getAllComments();
        assertEquals(allComments.get(0).getId(), comment.getId());
        assertEquals(allComments.size(), 1);
    }

    @Test
    void saveItemNullDescriptionTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                "space",
                                null,
                                true,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Не задано описание иснтрумента", exception.getMessage());
    }

    @Test
    void saveItemNullNameTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                null,
                                "",
                                true,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Не задано название инструмента", exception.getMessage());
    }

    @Test
    void saveItemEmptyDescriptionTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                "space",
                                "",
                                true,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Некорректно заданы поля в запросе", exception.getMessage());
    }

    @Test
    void saveItemAvailableTest() {
        Exception exception = assertThrows(IncorrectParameterException.class,
                () -> itemService.save(
                        new ItemDto(
                                null,
                                "Joe",
                                "Joe's thing",
                                null,
                                1L),
                        null,
                        1L)
        );
        assertEquals("Не определена доступность инструмента", exception.getMessage());
    }
}
