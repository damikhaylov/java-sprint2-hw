package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Task;

import java.util.List;

public interface HistoryManager {

    int HISTORY_MAX_SIZE = 10;

    void add(Task task);

    List<Task> getHistory();

}
