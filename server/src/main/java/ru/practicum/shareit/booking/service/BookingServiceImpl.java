package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Enums.Status;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingStorage;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.Enums.States.*;
import static ru.practicum.shareit.Enums.Status.*;
import static ru.practicum.shareit.util.Pagination.makePageRequest;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final BookingStorage bookingStorage;

    @Override
    @Transactional
    public BookingAllDto save(BookingControllerDto bookingControllerDto, ItemAllDto itemDto, Long id) {
        if (itemDto.getOwnerId().equals(id))
            throw new ObjectNotFoundException("Вещь с id = " + itemDto.getId() + " не можеь быть арендована");
        if (!itemDto.getAvailable())
            throw new IncorrectParameterException("Вещь с id = " + itemDto.getId() + " уже арендована");
        valid(bookingControllerDto);
        User booker = UserMapper.toUser(userService.get(id));
        Item item = ItemMapper.toItem(itemDto);
        Booking booking = BookingMapper.toBooking(bookingControllerDto);
        booking.setStatus(WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
        return BookingMapper.mapToBookingAllFieldsDto(bookingStorage.save(booking));
    }

    @Override
    @Transactional
    public BookingAllDto approve(Long bookingId, boolean approved, Long userId) {
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(
                () -> new ObjectNotFoundException("Такого бронирования не существует"));
        if (booking.getBooker().getId().equals(userId))
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не может одобрить заявку");
        if (!booking.getItem().getOwner().getId().equals(userId)
                || !booking.getStatus().equals(WAITING))
            throw new IncorrectParameterException("Бронирование не может быть обновлено");
        booking.setStatus(approved ? APPROVED : REJECTED);
        Booking savedBooking = bookingStorage.save(booking);
        return BookingMapper.mapToBookingAllFieldsDto(savedBooking);
    }

    @Override
    public List<BookingAllDto> getBookingsByOwner(Long userId, String state) {
        Stream<Booking> stream = null;
        User user = UserMapper.toUser(userService.get(userId));
        if (state == null || ALL.name().equals(state))
            stream = bookingStorage.findBookingsByItemOwnerIsOrderByStartDesc(user)
                    .stream();
        if (PAST.name().equals(state))
            stream = bookingStorage
                    .findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, now())
                    .stream();
        if (CURRENT.name().equals(state))
            stream = bookingStorage
                    .findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, now(), now())
                    .stream();
        if (FUTURE.name().equals(state))
            stream = bookingStorage
                    .findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now())
                    .stream();
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state)))
            stream = bookingStorage
                    .findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                    .stream();
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public List<BookingAllDto> getBookingsByOwner(Long userId, String state, Integer from, Integer size) {
        Stream<Booking> stream = null;
        PageRequest pageRequest = makePageRequest(from, size, Sort.by("start").descending());
        User user = UserMapper.toUser(userService.get(userId));
        if (state == null || state.equals(ALL.name())) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByItemOwnerIsOrderByStartDesc(user)
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByItemOwnerIsOrderByStartDesc(user, pageRequest)
                        .stream();
        }
        if (PAST.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now())
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (CURRENT.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now())
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByItemOwnerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (FUTURE.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now())
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByItemOwnerAndStartAfterOrderByStartDesc(user, now(), pageRequest)
                        .stream();
        }
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state))) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByItemOwnerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state), pageRequest)
                        .stream();
        }
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public List<BookingAllDto> getBookingsByItem(Long itemId, Long userId) {
        return bookingStorage.findBookingsByItem_IdAndItem_Owner_IdIsOrderByStart(
                        itemId, userId)
                .stream()
                .map(BookingMapper::mapToBookingAllFieldsDto)
                .collect(toList());
    }

    @Override
    public List<BookingAllDto> getAll(Long bookerId, String state) {
        Stream<Booking> stream = null;
        User user = UserMapper.toUser(userService.get(bookerId));
        if (state == null || ALL.name().equals(state))
            stream = bookingStorage
                    .findBookingsByBookerIsOrderByStartDesc(user)
                    .stream();
        if (PAST.name().equals(state))
            stream = bookingStorage
                    .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, LocalDateTime.now())
                    .stream();
        if (CURRENT.name().equals(state))
            stream = bookingStorage
                    .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now())
                    .stream();
        if (FUTURE.name().equals(state))
            stream = bookingStorage
                    .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, LocalDateTime.now())
                    .stream();
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state)))
            stream = bookingStorage
                    .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                    .stream();
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public List<BookingAllDto> getAll(Long bookerId, String state, Integer from, Integer size) {
        Stream<Booking> stream = null;
        PageRequest pageRequest = makePageRequest(from, size, Sort.by("start").descending());
        User user = UserMapper.toUser(userService.get(bookerId));
        if (state == null || ALL.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByBookerIsOrderByStartDesc(user)
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByBookerIsOrderByStartDesc(user, pageRequest)
                        .stream();
        }
        if (PAST.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, LocalDateTime.now())
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByBookerIsAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (CURRENT.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now())
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByBookerIsAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (FUTURE.name().equals(state)) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, LocalDateTime.now())
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByBookerIsAndStartIsAfterOrderByStartDesc(user, LocalDateTime.now(), pageRequest)
                        .stream();
        }
        if (Arrays.stream(Status.values()).anyMatch(bookingState -> bookingState.name().equals(state))) {
            if (pageRequest == null)
                stream = bookingStorage
                        .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state))
                        .stream();
            else
                stream = bookingStorage
                        .findBookingsByBookerIsAndStatusIsOrderByStartDesc(user, Status.valueOf(state), pageRequest)
                        .stream();
        }
        if (stream != null)
            return stream
                    .map(BookingMapper::mapToBookingAllFieldsDto)
                    .collect(toList());
        else
            throw new IncorrectParameterException("Unknown state: " + state);
    }

    @Override
    public BookingAllDto get(Long bookingId, Long userId) {
        Booking booking = bookingStorage.findById(bookingId).orElseThrow(
                () -> new ObjectNotFoundException("Такого бронирования не существует"));
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Данный пользователь не может получить информацию о бронировании");
        }
        return BookingMapper.mapToBookingAllFieldsDto(booking);
    }

    private void valid(BookingControllerDto bookingSavingDto) {
        if (bookingSavingDto.getStart() == null)
            throw new IncorrectParameterException("Не задана дата начала бронирования");
        if (bookingSavingDto.getEnd() == null)
            throw new IncorrectParameterException("Не задана дата окончания бронирования");
        if (bookingSavingDto.getStart().equals(bookingSavingDto.getEnd()))
            throw new IncorrectParameterException("Не задана дата начала бронирования");
        if (bookingSavingDto.getStart().toLocalDate().isBefore(LocalDate.now()))
            throw new IncorrectParameterException("Некорректная дата начала броинрования");
        if (bookingSavingDto.getEnd().isBefore(bookingSavingDto.getStart())
                || bookingSavingDto.getEnd().toLocalDate().isBefore(LocalDate.now()))
            throw new IncorrectParameterException("Некорректная дата бронирования");
    }
}
