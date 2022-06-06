package ru.yandex.practicum.tasktracker.manager;

import java.io.File;

public class Managers {
    public static final String DEFAULT_BACKUP_FILE_NAME = "tasks.csv";

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File(DEFAULT_BACKUP_FILE_NAME), true);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
