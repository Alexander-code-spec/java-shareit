package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final ItemService itemService;

    @PostMapping()
    public BookingAllDto save(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                              @RequestBody BookingControllerDto bookingControllerDto) {
        ItemAllDto item = itemService.get(bookingControllerDto.getItemId(), userId);
        return bookingService.save(bookingControllerDto, item, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingAllDto approve(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                 @RequestParam(required = false) boolean approved,
                                 @PathVariable Long bookingId) {
        return bookingService.approve(bookingId, approved, userId);
    }

    @GetMapping("/owner")
    public List<BookingAllDto> getBookingsByOwner(@RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId,
                                                  @RequestParam(required = false) String state,
                                                  @RequestParam(required = false) Integer from,
                                                  @RequestParam(required = false) Integer size) {
        return bookingService.getBookingsByOwner(userId, state, from, size);
    }

    @GetMapping()
    public List<BookingAllDto> getAll(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                      @RequestParam(required = false) String state,
                                      @RequestParam(required = false) Integer from,
                                      @RequestParam(required = false) Integer size) {
        return bookingService.getAll(userId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public BookingAllDto get(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                             @PathVariable Long bookingId) {
        return bookingService.get(bookingId, userId);
    }
}
