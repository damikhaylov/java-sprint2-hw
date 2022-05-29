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

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    int addTaskOfAnyType(Task task);

    Task addTaskOfAnyTypeAndReturnTask(Task task);

    boolean replaceTask(Task task);

    boolean replaceEpic(Epic epic);

    boolean replaceSubtask(Subtask subtask);

    void removeTaskOfAnyTypeById(int id);

    List<Task> getHistory();

    /**
     * Заменяет id задачи на заданный. Используется для замены в передаваемой в менеджер задаче id «по-умолчанию»
     * на очередной id, выдаваемый менеджером (задача при этом пересобирается с новым id)
     */
    // TODO: Комментарий для ревью (удалить после спринта 6) - Метод добавлен для обработки задач, передаваемых
    //  в менеджер без id (как в примере тестов техзадания)
    public static Task replaceTaskId(Task task, int id) {
        if (task.getClass() == Epic.class) {
            task = new Epic(id, task.getName(), task.getStatus(), task.getDescription());
        } else if (task.getClass() == Subtask.class) {
            task = new Subtask(id, task.getName(), task.getStatus(), task.getDescription(),
                    ((Subtask) task).getEpic());
        } else {
            task = new Task(id, task.getName(), task.getStatus(), task.getDescription());
        }
        return task;
    }
}
