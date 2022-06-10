package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;

public class TasksHelper {
    /**
     * Заменяет id задачи на заданный. Используется для замены в передаваемой в менеджер задаче id «по-умолчанию»
     * на очередной id, выдаваемый менеджером (задача при этом пересобирается с новым id)
     */

    public static Task replaceTaskId(Task task, int id) {
        return new Task(id, task.getName(), task.getStatus(), task.getDescription(),
                task.getStartTime(), task.getDuration());
    }

    public static Epic replaceTaskId(Epic epic, int id) {
        return new Epic(id, epic.getName(), epic.getStatus(), epic.getDescription(),
                epic.getStartTime(), epic.getDuration(), epic.getEndTime());
    }

    public static Subtask replaceTaskId(Subtask subtask, int id) {
        return new Subtask(id, subtask.getName(), subtask.getStatus(), subtask.getDescription(),
                subtask.getStartTime(), subtask.getDuration(), subtask.getEpicId());
    }
}