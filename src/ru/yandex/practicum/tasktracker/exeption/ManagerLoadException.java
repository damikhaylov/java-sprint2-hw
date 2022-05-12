package ru.yandex.practicum.tasktracker.exeption;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message) {
        super(message);
    }

    public ManagerLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerLoadException(Throwable cause) {
        super(cause);
    }
}
