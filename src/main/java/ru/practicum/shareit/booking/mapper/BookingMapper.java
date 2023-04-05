package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

public class BookingMapper {
    public static Booking toBooking(BookingControllerDto bookingDto) {
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    public static BookingAllDto mapToBookingAllFieldsDto(Booking booking) {
        return BookingAllDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem() != null ? ItemMapper.toItemDto(booking.getItem()) : null)
                .booker(booking.getBooker() != null ? UserMapper.toUserDto(booking.getBooker()) : null)
                .status(booking.getStatus().name())
                .build();
    }
}
