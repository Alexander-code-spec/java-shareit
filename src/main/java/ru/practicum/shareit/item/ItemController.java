package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping()
    public ItemDto save(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                        @RequestBody ItemDto itemDto) {
        return itemService.save(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable Integer itemId) {
        return itemService.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                                @PathVariable Integer itemId) {
        return itemService.get(itemId);
    }

    @GetMapping()
    public List<ItemDto> getAllItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId) {
        return itemService.getAll(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId,
                                @RequestParam String text) {
        return itemService.getByText(text.toLowerCase());
    }

}
