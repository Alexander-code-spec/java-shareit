package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping()
    public List<ItemRequestDto> getItemRequests(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemRequestService.getAllItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                   @RequestParam(required = false) Integer from,
                                                   @RequestParam(required = false) Integer size) {
        return itemRequestService.getAllItemRequests(from, size, userId);
    }

    @PostMapping()
    public ItemRequestDto createItemRequest(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                            @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.save(itemRequestDto, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                         @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(requestId, userId);
    }
}
