package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    int getNextTaskId();

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Subtask> getEpicSubtasks(int id);

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    int addTaskOfAnyType(Task task);

    boolean replaceTask(Task task);

    boolean replaceEpic(Epic epic);

    boolean replaceSubtask(Subtask subtask);

    boolean removeTaskOfAnyTypeById(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}