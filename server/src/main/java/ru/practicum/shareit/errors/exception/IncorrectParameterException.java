package ru.practicum.shareit.errors.exception;

public class IncorrectParameterException extends RuntimeException {
    public IncorrectParameterException(String parameter) {
        super(parameter);
    }
}
