package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> history;

    InMemoryHistoryManager() {
        history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            history.add(task);
        }
    }

    @Override
    public void remove(int id) {

    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
