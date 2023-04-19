package ru.practicum.shareit.item.service;

import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.item.model.Item;
import org.junit.jupiter.api.BeforeEach;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.equalTo;
import static java.time.LocalDateTime.*;
import static org.hamcrest.Matchers.*;
import static java.util.List.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private final ItemRequestService itemRequestService;
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemService itemService;
    private UserDto userDto;
    private ItemDto itemDto;

    @BeforeEach
    void initialize() {
        userDto = userService.save(
                new UserDto(
                        null,
                        "Joe",
                        "joe@mail.com")
        );
        itemDto = itemService.save(
                new ItemDto(null,
                        "Pen",
                        "Blue Pen",
                        true,
                        null),
                null,
                userDto.getId()
        );
    }

    private CommentDto saveCommentDto(String commentText, UserDto userDto) {
        UserDto booker = userService.save(userDto);
        bookingService.save(
                new BookingControllerDto(
                        null,
                        now().minusSeconds(2),
                        now().minusSeconds(1),
                        itemDto.getId(),
                        booker.getId(),
                        null),
                new ItemAllDto(
                        itemDto.getId(),
                        itemDto.getName(),
                        itemDto.getDescription(),
                        true,
                        userDto.getId(),
                        null,
                        null,
                        null,
                        of()),
                booker.getId()
        );
        var commentDto = new CommentDto(
                null,
                commentText,
                itemDto.getId(),
                booker.getName(),
                now()
        );
        return itemService.createComment(
                commentDto,
                commentDto.getItemId(),
                booker.getId());
    }


    @Test
    void saveTest() {
        Item item = entityManager.createQuery(
                "SELECT item " +
                        "FROM Item item",
                Item.class).getSingleResult();
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getId(), notNullValue());
    }

    @Test
    void updateTest() {
        ItemDto dto = new ItemDto(
                itemDto.getId(),
                "Bear",
                "Soft toy",
                false,
                null
        );
        itemService.update(dto, dto.getId(), userDto.getId());
        Item item = entityManager.createQuery(
                "SELECT item " +
                        "FROM Item item",
                Item.class).getSingleResult();
        assertThat(item.getDescription(), equalTo(dto.getDescription()));
        assertThat(item.getAvailable(), equalTo(dto.getAvailable()));
        assertThat(item.getName(), equalTo(dto.getName()));
        assertThat(item.getId(), notNullValue());
    }

    @Test
    void getTest() {
        ItemAllDto itemAllFieldsDto = itemService.get(itemDto.getId(), userDto.getId());
        Item item = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.id = :id " +
                                "AND item.owner.id = :ownerId",
                        Item.class)
                .setParameter("ownerId", userDto.getId())
                .setParameter("id", itemDto.getId())
                .getSingleResult();
        assertThat(item.getDescription(), equalTo(itemAllFieldsDto.getDescription()));
        assertThat(item.getOwner().getId(), equalTo(itemAllFieldsDto.getOwnerId()));
        assertThat(item.getAvailable(), equalTo(itemAllFieldsDto.getAvailable()));
        assertThat(item.getName(), equalTo(itemAllFieldsDto.getName()));
        assertThat(item.getId(), notNullValue());
    }

    @Test
    void getAllTest() {
        itemDto = itemService.save(
                new ItemDto(
                        null,
                        "Doll",
                        "Tall doll",
                        true,
                        null),
                null,
                userDto.getId()
        );
        List<ItemAllDto> allItems = itemService.getAll(userDto.getId(), 0, 2);
        List<Item> items = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.owner.id = :ownerId",
                        Item.class)
                .setParameter("ownerId", userDto.getId())
                .getResultList();
        assertThat(items.get(0).getId(), equalTo(allItems.get(0).getId()));
        assertThat(items.size(), equalTo(allItems.size()));
    }

    @Test
    void searchNotAvailableItemTest() {
        itemDto = itemService.save(
                new ItemDto(
                        null,
                        "Truck",
                        "Big truck",
                        false,
                        null),
                null,
                userDto.getId()
        );
        List<ItemDto> itemsDto = itemService.getByText("truck", null, 0, 2);
        List<Item> items = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.available = TRUE AND (UPPER(item.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
                                "OR UPPER(item.description) LIKE UPPER(CONCAT('%', :text, '%')))",
                        Item.class)
                .setParameter("text", "car")
                .getResultList();
        assertThat(items.size(), equalTo(itemsDto.size()));
        assertThat(items, empty());
    }

    @Test
    void searchTest() {
        itemDto = itemService.save(
                new ItemDto(
                        1L,
                        "Car",
                        "Red car",
                        true,
                        null),
                null,
                userDto.getId()
        );
        List<ItemDto> itemsDto = itemService.getByText("car", null, 0, 2);
        List<Item> items = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.available = TRUE AND (UPPER(item.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
                                "OR UPPER(item.description) LIKE UPPER(CONCAT('%', :text, '%')))",
                        Item.class)
                .setParameter("text", "car")
                .getResultList();
        assertThat(items.get(0).getId(), equalTo(itemsDto.get(0).getId()));
        assertThat(items.size(), equalTo(itemsDto.size()));
    }

    @Test
    void getAllCommentsTest() {
        saveCommentDto(
                "Winter",
                new UserDto(
                        12L,
                        "Richard",
                        "richard@mail.com")
        );
        saveCommentDto(
                "Spring",
                new UserDto(
                        13L,
                        "Bethany",
                        "bethany@mail.com")
        );
        var allComments = itemService.getAllComments();
        var comments = entityManager.createQuery(
                        "SELECT comment " +
                                "FROM Comment comment",
                        Comment.class)
                .getResultList();
        assertThat(comments.size(), equalTo(allComments.size()));
        assertThat(comments.size(), equalTo(2));
        assertThat(comments, notNullValue());
    }

    @Test
    void getItemsByRequestIdTest() {
        var requester = userService.save(
                new UserDto(
                        null,
                        "Abby",
                        "abby@mail.com")
        );
        var itemRequestDto = itemRequestService.save(
                new ItemRequestDto(
                        null,
                        "I need it",
                        requester.getId(),
                        now(),
                        of()),
                requester.getId()
        );
        itemService.save(
                new ItemDto(
                        null,
                        "Thing",
                        "Little thing",
                        true,
                        null),
                itemRequestDto,
                userDto.getId()
        );
        List<ItemDto> itemsByRequestId = itemService.getItemsByRequestId(itemRequestDto.getId());
        List<Item> itemsByRequest = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.request.id = :requestId",
                        Item.class)
                .setParameter("requestId", itemRequestDto.getId())
                .getResultList();
        assertThat(itemsByRequestId.size(), equalTo(itemsByRequest.size()));
        assertThat(itemsByRequestId.size(), equalTo(1));
        assertThat(itemsByRequestId, notNullValue());
    }

    @Test
    void searchEmptyResultTest() {
        itemDto = itemService.save(
                new ItemDto(
                        null,
                        "House",
                        "Big house",
                        true,
                        null),
                null,
                userDto.getId()
        );
        List<ItemDto> itemsDto = itemService.getByText("nothing", null, 0, 2);
        List<Item> items = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.available = TRUE AND (UPPER(item.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
                                "OR UPPER(item.description) LIKE UPPER(CONCAT('%', :text, '%')))",
                        Item.class)
                .setParameter("text", "nothing")
                .getResultList();
        assertThat(items.size(), equalTo(itemsDto.size()));
        assertThat(items, empty());
    }

    @Test
    void saveCommentTest() {
        CommentDto commentDto = saveCommentDto(
                "Hello there",
                new UserDto(
                        15L,
                        "Douglas",
                        "douglas@mail.com")
        );
        Comment comment = entityManager.createQuery(
                "SELECT comment " +
                        "FROM Comment comment",
                Comment.class).getSingleResult();
        assertThat(comment.getAuthor().getName(), equalTo(commentDto.getAuthorName()));
        assertThat(comment.getItem().getId(), equalTo(commentDto.getItemId()));
        assertThat(comment.getText(), equalTo(commentDto.getText()));
        assertThat(comment.getId(), equalTo(commentDto.getId()));
        assertThat(comment.getId(), notNullValue());
    }

    @Test
    void getItemsByRequestIdEmptyResultTest() {
        UserDto requester = userService.save(
                new UserDto(
                        null,
                        "Zoe",
                        "zoe@mail.com")
        );
        ItemRequestDto itemRequestDto = itemRequestService.save(
                new ItemRequestDto(
                        null,
                        "I need it",
                        requester.getId(),
                        now(),
                        of()),
                requester.getId()
        );
        List<ItemDto> itemsByRequestId = itemService.getItemsByRequestId(itemRequestDto.getId());
        List<Item> itemsByRequest = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.request.id = :requestId",
                        Item.class)
                .setParameter("requestId", itemRequestDto.getId())
                .getResultList();
        assertThat(itemsByRequestId.size(), equalTo(itemsByRequest.size()));
        assertThat(itemsByRequestId, empty());
    }

    @Test
    void getAllCommentsIdTest() {
        saveCommentDto(
                "Hello there",
                new UserDto(
                        15L,
                        "Douglas",
                        "douglas@mail.com")
        );
        List<CommentDto> allComments = itemService.getAllComments();
        List<Comment> comments = entityManager.createQuery(
                        "SELECT comment " +
                                "FROM Comment comment " +
                                "WHERE comment.item.id = :itemId",
                        Comment.class)
                .setParameter("itemId", itemDto.getId())
                .getResultList();
        assertThat(comments.get(0).getId(), equalTo(allComments.get(0).getId()));
        assertThat(comments.size(), equalTo(allComments.size()));
        assertThat(comments.size(), equalTo(1));
        assertThat(comments, notNullValue());
    }

    @Test
    void getNotFoundExceptionTest() {
        final long id = 7L;
        Exception exception = assertThrows(ObjectNotFoundException.class,
                () -> itemService.get(id, userDto.getId()));
        assertEquals("Вещь с id " + id + " не найдена", exception.getMessage());
    }
}
