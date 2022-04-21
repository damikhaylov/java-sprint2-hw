package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task);

    void remove(int id);

    List<Task> getHistory();

}
