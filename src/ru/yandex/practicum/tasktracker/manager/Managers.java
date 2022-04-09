package ru.yandex.practicum.tasktracker.manager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
