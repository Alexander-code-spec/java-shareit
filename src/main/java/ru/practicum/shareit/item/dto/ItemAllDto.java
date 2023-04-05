package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
public class ItemAllDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;
    private Long requestId;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
}
