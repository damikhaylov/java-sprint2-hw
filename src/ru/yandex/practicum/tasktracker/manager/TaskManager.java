package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    public int getNextTaskId();

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    int addTaskOfAnyType(Task task);

    boolean replaceTask(Task task);

    boolean replaceEpic(Epic epic);

    boolean replaceSubtask(Subtask subtask);

    void deleteTaskOfAnyTypeById(int id);

    List<Task> getHistory();

}
