package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;
import ru.practicum.shareit.errors.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.List.*;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    private final String headerSharerUserId = "X-Sharer-User-Id";
    @MockBean
    ItemRequestService itemRequestService;
    @MockBean
    ItemService itemService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("qwerty")
            .itemId(1L)
            .authorName("Paul")
            .created(now())
            .build();

    private final ItemAllDto itemExtendedDto = new ItemAllDto(
            1L,
            "blue pen",
            "my blue pen",
            true,
            1L,
            null,
            null,
            null,
            of(commentDto));

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("pen")
            .description("blue pen")
            .available(true)
            .requestId(1L)
            .build();

    @Test
    void saveTest() throws Exception {
        when(itemService.save(any(), any(), anyLong()))
                .thenReturn(itemDto);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void saveItemRequestDtoIsNullTest() throws Exception {
        ItemDto itemDto2 = ItemDto.builder()
                .id(1L)
                .name("pen")
                .description("blue pen")
                .available(true)
                .requestId(null)
                .build();

        when(itemService.save(any(), any(), anyLong()))
                .thenReturn(itemDto2);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto2))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$.id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto2.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsTest() throws Exception {
        when(itemService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(of(itemExtendedDto));
        mvc.perform(get("/items")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].description", is(itemExtendedDto.getDescription())))
                .andExpect(jsonPath("$[0].id", is(itemExtendedDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemExtendedDto.getName())))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemTest() throws Exception {
        when(itemService.get(any(), anyLong()))
                .thenReturn(itemExtendedDto);
        mvc.perform(get("/items/{itemId}", 1)
                        .header(headerSharerUserId, 1)
                )
                .andExpect(jsonPath("$.description", is(itemExtendedDto.getDescription())))
                .andExpect(jsonPath("$.id", is(itemExtendedDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemExtendedDto.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void updateTest() throws Exception {
        when(itemService.update(any(), anyLong(), anyLong()))
                .thenReturn(itemDto);
        mvc.perform(patch("/items/{itemId}", 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                )
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void saveCommentTest() throws Exception {
        when(itemService.createComment(any(CommentDto.class), anyLong(), anyLong()))
                .thenReturn(commentDto);
        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(status().isOk());
    }

    @Test
    void searchTest() throws Exception {
        when(itemService.getByText(anyString(), anyLong(), anyInt(), anyInt()))
                .thenReturn(of(itemDto));
        mvc.perform(get("/items/search")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0")
                        .param("text", "")
                )
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void saveValidationExceptionTest() throws Exception {
        when(itemService.save(any(), any(), anyLong()))
                .thenThrow(IncorrectParameterException.class);
        mvc.perform(post("/items")
                        .header(headerSharerUserId, 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNotFoundExceptionTest() throws Exception {
        when(itemService.update(any(), anyLong(), anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        mvc.perform(patch("/items/{itemId}", 1)
                        .header(headerSharerUserId, 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotFoundExceptionTest() throws Exception {
        when(itemService.get(any(), anyLong()))
                .thenThrow(ObjectNotFoundException.class);
        mvc.perform(get("/items/{itemId}", 1)
                        .header(headerSharerUserId, 1)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void saveCommentValidationExceptionTest() throws Exception {
        when(itemService.createComment(any(CommentDto.class), anyLong(), anyLong()))
                .thenThrow(IncorrectParameterException.class);
        mvc.perform(post("/items/{itemId}/comment", 1)
                        .header(headerSharerUserId, 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
