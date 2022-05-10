package ru.yandex.practicum.tasktracker.exeption;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message) {
        super(message);
    }
}
