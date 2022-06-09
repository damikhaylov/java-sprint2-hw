package ru.yandex.practicum.tasktracker.manager;

public class Managers {
    public static final String DEFAULT_BACKUP_FILE_NAME = "tasks.csv";
    public static final String DEFAULT_URL = "http://localhost:8078";

    public static TaskManager getDefault() {
        return new HTTPTaskManager(DEFAULT_URL);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
