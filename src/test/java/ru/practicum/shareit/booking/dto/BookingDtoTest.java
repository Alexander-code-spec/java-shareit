package ru.practicum.shareit.booking.dto;

import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.shareit.Enums.Status.WAITING;
import static org.assertj.core.api.Assertions.assertThat;
import static java.time.LocalDateTime.of;
import static java.time.Month.DECEMBER;

@JsonTest
class BookingDtoTest {
    private final ItemShortDto itemDto = new ItemShortDto(1L, "Pen");
    private final LocalDateTime startTime = of(2000, DECEMBER, 3, 0, 5, 10);
    private final LocalDateTime endTime = of(2000, DECEMBER, 5, 0, 5, 10);
    private final UserShortDto userDto = new UserShortDto(1L, "Lora");
    @Autowired
    private JacksonTester<BookingAllDto> bookingAllFieldsDtoJacksonTester;
    @Autowired
    private JacksonTester<BookingControllerDto> bookingSavingDtoJacksonTester;
    @Autowired
    private JacksonTester<BookingDto> bookingDtoJacksonTester;

    private final BookingAllDto bookingAllFieldsDto = BookingAllDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .item(itemDto)
            .booker(userDto)
            .status(WAITING)
            .build();

    private final BookingControllerDto bookingSavingDto = BookingControllerDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .itemId(1L)
            .booker(1L)
            .status(WAITING.name())
            .build();

    private final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .bookerId(1L)
            .build();

    @Test
    void bookingDtoJacksonTesterTest() throws Exception {
        JsonContent<BookingDto> jsonContent = bookingDtoJacksonTester.write(bookingDto);
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingDto.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.bookerId")
                .isEqualTo(bookingDto.getBookerId().intValue());
    }

    @Test
    void bookingSavingDtoJacksonTesterTest() throws Exception {
        JsonContent<BookingControllerDto> jsonContent = bookingSavingDtoJacksonTester.write(bookingSavingDto);
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingSavingDto.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingSavingDto.getStart().toString());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingSavingDto.getEnd().toString());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(bookingSavingDto.getItemId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.booker")
                .isEqualTo(bookingSavingDto.getBooker().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingSavingDto.getStatus());
    }

    @Test
    void bookingAllFieldsDtoJacksonTesterTest() throws Exception {
        JsonContent<BookingAllDto> jsonContent = bookingAllFieldsDtoJacksonTester.write(bookingAllFieldsDto);
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(bookingAllFieldsDto.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingAllFieldsDto.getStart().toString());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingAllFieldsDto.getEnd().toString());
        assertThat(jsonContent)
                .extractingJsonPathMapValue("$.item").isNotNull();
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.item.id")
                .isEqualTo(bookingAllFieldsDto.getItem().getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.item.name")
                .isEqualTo(bookingAllFieldsDto.getItem().getName());
        assertThat(jsonContent)
                .extractingJsonPathMapValue("$.booker")
                .isNotNull();
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.booker.id")
                .isEqualTo(bookingAllFieldsDto.getBooker().getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.booker.name")
                .isEqualTo(bookingAllFieldsDto.getBooker().getName());
    }
}
