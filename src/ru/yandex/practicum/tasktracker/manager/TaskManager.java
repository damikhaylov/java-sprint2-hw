package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;

import java.util.ArrayList;

public interface TaskManager {

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    int addTaskOfAnyType(Task task);

    boolean replaceTask(Task task);

    boolean replaceEpic(Epic epic);

    boolean replaceSubtask(Subtask subtask);

    void deleteTaskOfAnyTypeById(int id);

}
