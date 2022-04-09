package ru.yandex.practicum.tasktracker.manager;

public class Managers {
    private final static HistoryManager historyManager = new InMemoryHistoryManager();

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }
}
