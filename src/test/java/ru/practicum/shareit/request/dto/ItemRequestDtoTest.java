package ru.practicum.shareit.request.dto;

import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;
import static java.time.LocalDateTime.*;
import static java.util.List.*;

@JsonTest
class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoJacksonTester;

    @Test
    void itemRequestDtoTest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto(
                1L,
                "Red carpet",
                null,
                now(),
                of()
        );
        JsonContent<ItemRequestDto> jsonContent = itemRequestDtoJacksonTester.write(itemRequestDto);
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.description")
                .isEqualTo(itemRequestDto.getDescription());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.requestorId")
                .isEqualTo(itemRequestDto.getRequesterId());
        assertThat(jsonContent)
                .extractingJsonPathArrayValue("$.items")
                .isNullOrEmpty();
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemRequestDto.getId().intValue());
    }
}
