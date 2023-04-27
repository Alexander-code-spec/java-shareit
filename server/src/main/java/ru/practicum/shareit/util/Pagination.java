package ru.practicum.shareit.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.errors.exception.IncorrectParameterException;

import static org.springframework.data.domain.PageRequest.*;

public class Pagination {
    public static PageRequest makePageRequest(Integer from, Integer size, Sort sort) {
        if (size == null || from == null) return null;
        if (size <= 0 || from < 0) throw new IncorrectParameterException("size <= 0 || from < 0");
        return of(from / size, size, sort);
    }
}
