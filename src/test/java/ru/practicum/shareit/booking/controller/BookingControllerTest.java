package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingAllDto;
import ru.practicum.shareit.booking.dto.BookingControllerDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserShortDto;
import java.time.LocalDateTime;
import java.util.List;
import static java.util.Calendar.DECEMBER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.Enums.Status.WAITING;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.http.MediaType.*;
import static java.nio.charset.StandardCharsets.*;
import static org.hamcrest.Matchers.*;
import static java.util.List.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    private final ItemShortDto itemShortDto = new ItemShortDto(1L, "Pen");
    private final LocalDateTime startTime = LocalDateTime.of(2000, DECEMBER, 3, 0, 5, 10);
    private final LocalDateTime endTime = LocalDateTime.of(2000, DECEMBER, 5, 0, 5, 10);
    private final UserShortDto userShortDto = new UserShortDto(1L, "Lora");
    private final String headerSharerUserId = "X-Sharer-User-Id";
    @MockBean
    BookingService bookingService;
    @MockBean
    ItemService itemService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final BookingAllDto bookingAllFieldsDto = BookingAllDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .item(itemShortDto)
            .booker(userShortDto)
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

    @Test
    void getAllBookingsTest() throws Exception {
        when(bookingService.getAll(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingAllFieldsDto));
        mvc.perform(get("/bookings")
                        .header(headerSharerUserId, 1)
                        .param("state", "All")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].start", is(bookingAllFieldsDto.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingAllFieldsDto.getEnd().toString())))
                .andExpect(jsonPath("$[0].id", is(bookingAllFieldsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker", notNullValue()))
                .andExpect(jsonPath("$[0].item", notNullValue()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwnerIdTest() throws Exception {
        when(bookingService.getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(of(bookingAllFieldsDto));
        mvc.perform(get("/bookings/owner")
                        .header(headerSharerUserId, 1)
                        .param("state", "All")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].start", is(bookingAllFieldsDto.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingAllFieldsDto.getEnd().toString())))
                .andExpect(jsonPath("$[0].id", is(bookingAllFieldsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].booker", notNullValue()))
                .andExpect(jsonPath("$[0].item", notNullValue()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void saveTest() throws Exception {
        when(bookingService.save(any(), any(), anyLong()))
                .thenReturn(bookingAllFieldsDto);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingSavingDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.start", is(bookingAllFieldsDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingAllFieldsDto.getEnd().toString())))
                .andExpect(jsonPath("$.id", is(bookingAllFieldsDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void approveTest() throws Exception {
        when(bookingService.approve(anyLong(), anyBoolean(), anyLong()))
                .thenReturn(bookingAllFieldsDto);
        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(mapper.writeValueAsString(bookingAllFieldsDto))
                        .param("approved", "true")
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.start", is(bookingAllFieldsDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingAllFieldsDto.getEnd().toString())))
                .andExpect(jsonPath("$.id", is(bookingAllFieldsDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByItemTest() throws Exception {
        when(bookingService.get(anyLong(), anyLong()))
                .thenReturn(bookingAllFieldsDto);
        mvc.perform(get("/bookings/{bookingId}", 1)
                        .header(headerSharerUserId, 1))
                .andExpect(jsonPath("$.start", is(bookingAllFieldsDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingAllFieldsDto.getEnd().toString())))
                .andExpect(jsonPath("$.id", is(bookingAllFieldsDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBookingsValidationExceptionTest() throws Exception {
        when(bookingService.getAll(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(IncorrectParameterException.class);
        mvc.perform(get("/bookings")
                        .header(headerSharerUserId, 1)
                        .param("state", "Alll")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsByOwnerValidationExceptionTest() throws Exception {
        when(bookingService.getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(IncorrectParameterException.class);
        mvc.perform(get("/bookings/owner")
                        .header(headerSharerUserId, 1)
                        .param("state", "All")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveNotFoundExceptionTest() throws Exception {
        when(bookingService.save(any(), any(), anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingSavingDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void saveValidationExceptionTest() throws Exception {
        when(bookingService.save(any(), any(), anyLong()))
                .thenThrow(IncorrectParameterException.class);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingSavingDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveValidationExceptionTest() throws Exception {
        when(bookingService.approve(anyLong(), anyBoolean(), anyLong()))
                .thenThrow(IncorrectParameterException.class);
        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(mapper.writeValueAsString(bookingAllFieldsDto))
                        .param("approved", "true")
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingByIdNotFoundExceptionTest() throws Exception {
        when(bookingService.get(anyLong(), anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        mvc.perform(get("/bookings/{bookingId}", 1)
                        .header(headerSharerUserId, 1)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void approveNotFoundExceptionTest() throws Exception {
        when(bookingService.approve(anyLong(), anyBoolean(), anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(mapper.writeValueAsString(bookingAllFieldsDto))
                        .param("approved", "true")
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllInternalServerErrorTest() throws Exception {
        when(bookingService.getAll(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(IncorrectParameterException.class);
        mvc.perform(get("/bookings")
                        .header(headerSharerUserId, 1)
                        .param("state", "All")
                        .param("size", "10000000000")
                        .param("from", "0")
                )
                .andExpect(status().is5xxServerError());
    }
}
