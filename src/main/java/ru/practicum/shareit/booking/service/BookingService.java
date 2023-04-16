package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.item.dto.ItemAllDto;

import java.util.List;

public interface BookingService  {
    BookingAllDto save(BookingControllerDto booking, ItemAllDto itemDto, Long id);

    BookingAllDto approve(Long id, boolean approved, Long userId);

    List<BookingAllDto> getBookingsByOwner(Long userId, String state);

    List<BookingAllDto> getBookingsByItem(Long itemId, Long userId);

    List<BookingAllDto> getAll(Long id, String state);

    BookingAllDto get(Long id, Long userId);
}
