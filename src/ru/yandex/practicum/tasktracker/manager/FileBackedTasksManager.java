package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    public void save() {

    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public int addTaskOfAnyType(Task task) {
        int id = super.addTaskOfAnyType(task);
        save();
        return id;
    }

    @Override
    public boolean replaceTask(Task task) {
        boolean isSuccessfullyReplacing = super.replaceTask(task);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public boolean replaceEpic(Epic epic) {
        boolean isSuccessfullyReplacing = super.replaceEpic(epic);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public boolean replaceSubtask(Subtask subtask) {
        boolean isSuccessfullyReplacing = super.replaceSubtask(subtask);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public void removeTaskOfAnyTypeById(int id) {
        super.removeTaskOfAnyTypeById(id);
        save();
    }

    static public String toString(Task task) {
        final char sep = ',';
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(task.getId()).append(sep);
        stringBuilder.append(task.getClass().getSimpleName().toUpperCase()).append(sep);
        stringBuilder.append(task.getName()).append(sep);
        stringBuilder.append(task.getStatus()).append(sep);
        stringBuilder.append(task.getDescription()).append(sep);
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            stringBuilder.append(subtask.getEpic().getId());
        }
        return stringBuilder.toString();
    }
}