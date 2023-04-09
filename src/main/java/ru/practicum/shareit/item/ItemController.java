package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllDto;
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
    public ItemDto save(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                        @RequestBody ItemDto itemDto) {
        return itemService.save(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable Long itemId) {
        return itemService.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemAllDto get(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId) {
        return itemService.get(itemId, userId);
    }

    @GetMapping()
    public List<ItemAllDto> getAllItems(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemService.getAll(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.getByText(text.toLowerCase());
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestBody CommentDto commentDto,
                                    @PathVariable Long itemId,
                                    @RequestHeader(value = "X-Sharer-User-Id", required = false)
                                    Long userId) {
        return itemService.createComment(commentDto, itemId, userId);
    }

}
