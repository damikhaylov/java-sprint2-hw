package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Task;

import java.util.List;

public interface HistoryManager {

    int HISTORY_MAX_SIZE = 10; //FIXME удалить константу после реализации неограниченной истории

    void add(Task task);

    void remove(int id);

    List<Task> getHistory();

}
