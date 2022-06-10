package ru.yandex.practicum.tasktracker.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.practicum.tasktracker.server.LocalDateTimeAdapter;

import java.time.LocalDateTime;

public class Managers {
    public static final String DEFAULT_BACKUP_FILE_NAME = "tasks.csv";
    public static final String DEFAULT_URL = "http://localhost:8078";

    public static TaskManager getDefault() {
        return new HTTPTaskManager(DEFAULT_URL, false);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        return gsonBuilder.create();
    }
}
