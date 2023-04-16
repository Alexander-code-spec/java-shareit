package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Long> {

    List<Comment> findByItem(Item item, Sort sort);

    List<Comment> findByItemIn(Collection<Item> items, Sort sort);
}
