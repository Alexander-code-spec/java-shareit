package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentStorage;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

import static ru.practicum.shareit.Enums.States.PAST;


@Slf4j
@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;
    private final CommentStorage commentStorage;
    private final BookingService bookingService;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage,
                           UserService userService,
                           CommentStorage commentStorage,
                           BookingService bookingService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
        this.commentStorage = commentStorage;
        this.bookingService = bookingService;
    }

    @Override
    public ItemAllDto get(Long id, Long userId) {
        Item item = itemStorage.findById(id).orElseThrow(
                () -> new ObjectNotFoundException("Вещь с id " + id + " не найдена"));
        Map<Long, List<CommentDto>> comments = getAllComments().stream()
                .collect(groupingBy(CommentDto::getItemId));
        if (item.getOwner().getId().equals(userId)) {
            List<BookingAllDto> bookings = bookingService.getBookingsByItem(item.getId(), userId);
            return ItemMapper.toItemAllFieldsDto(item,
                    getLastItem(bookings),
                    getNextItem(bookings),
                    comments.get(item.getId()));
        } else {
            return ItemMapper.toItemAllFieldsDto(item,
                    getLastItem(null),
                    getNextItem(null),
                    comments.get(item.getId()));
        }


    }

    @Override
    @Transactional
    public ItemDto save(ItemDto itemDto, Long userId) {
        valid(itemDto);
        User owner = UserMapper.toUser(userService.get(userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        if (userId == null) {
            throw new IncorrectParameterException("Id пользователя не задан!");
        }

        Item item = itemStorage.getReferenceById(id);

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
    public List<ItemAllDto> getAll(Long id) {
        Optional<User> owner = Optional.of(UserMapper.toUser(userService.get(id)));
        if (owner.isPresent()) {
            Map<Long, List<CommentDto>> comments = getAllComments().stream()
                    .collect(groupingBy(CommentDto::getItemId));
            Map<Long, List<BookingAllDto>> bookings = bookingService.getBookingsByOwner(id, null).stream()
                    .collect(groupingBy((BookingAllDto bookingExtendedDto) -> bookingExtendedDto.getItem().getId()));
            return itemStorage.findAllByOwner_IdIs(id).stream()
                    .map(item -> getItemAllFieldsDto(comments, bookings, item))
                    .collect(toList());
        } else {
            throw new ObjectNotFoundException("Пользователь с id" + id + "не найден");
        }

    }

    @Override
    public List<ItemDto> getByText(String text) {
        if (text.isBlank()) return Collections.emptyList();
        return itemStorage.getAllText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto,
                                    Long itemId,
                                    Long userId) {
        if (commentDto.getText() == null || commentDto.getText().isBlank())
            throw new IncorrectParameterException("Текст комментария не может быть пустым");
        Item item = itemStorage.findById(itemId).orElseThrow(
                () -> new ObjectNotFoundException("Вещь с id = " + itemId + " не найдена"));
        User user = UserMapper.toUser(userService.get(userId));
        List<BookingAllDto> bookings = bookingService.getAll(userId, PAST.name());
        if (bookings.isEmpty()) throw new IncorrectParameterException("Нельзя оставить комментарий");
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment save = commentStorage.save(comment);
        return CommentMapper.toCommentDto(save);
    }

    @Override
    public List<CommentDto> getAllComments() {
        return commentStorage.findAll()
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private ItemAllDto getItemAllFieldsDto(Map<Long, List<CommentDto>> comments,
                                           Map<Long, List<BookingAllDto>> bookings,
                                           Item item) {
            return ItemMapper.toItemAllFieldsDto(item,
                    getLastItem(bookings.get(item.getId())),
                    getNextItem(bookings.get(item.getId())),
                    comments.get(item.getId()));
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

    private BookingAllDto getNextItem(List<BookingAllDto> bookings) {
        return bookings != null
                ? bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(BookingAllDto::getEnd)).orElse(null)
                : null;
    }

    private BookingAllDto getLastItem(List<BookingAllDto> bookings) {
        return bookings != null
                ? bookings.stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(BookingAllDto::getEnd)).orElse(null)
                : null;
    }
}
