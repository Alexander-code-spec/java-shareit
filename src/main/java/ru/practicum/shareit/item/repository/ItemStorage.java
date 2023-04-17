package ru.practicum.shareit.item.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {

    List<Item> findAllByOwner_IdIs(Long ownerId, Pageable pageable);

    @Query("select item from Item item " +
            "where item.available = TRUE " +
            "and (upper(item.name) " +
            "like upper(concat('%', ?1, '%')) " +
            "or upper(item.description) " +
            "like upper(concat('%', ?1, '%')))")
    List<Item> getAllText(String text);

    @Query("select item from Item item " +
            "where item.available = TRUE " +
            "and (upper(item.name) " +
            "like upper(concat('%', ?1, '%')) " +
            "or upper(item.description) " +
            "like upper(concat('%', ?1, '%')))")
    List<Item> getAllText(String text, Pageable pageable);

    List<Item> findAllByOwner_IdIs(Long ownerId);

    List<Item> findAllByRequestIn(List<ItemRequest> requests);

    List<Item> findAllByRequest_IdIs(Long requestId);
}
