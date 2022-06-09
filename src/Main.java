import ru.yandex.practicum.tasktracker.manager.*;
import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;
import ru.yandex.practicum.tasktracker.model.TaskStatus;
import ru.yandex.practicum.tasktracker.server.KVServer;

import java.io.IOException;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws IOException {
        KVServer server = new KVServer();
        server.start();

        HTTPTaskManager taskManager = new HTTPTaskManager("http://localhost:8078");
        HTTPTaskManager newTaskManager = new HTTPTaskManager("http://localhost:8078");

        Task taskA = new Task("Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.of(2022, 6, 1, 12, 45), 15);
        Task taskB = new Task("Task B", TaskStatus.IN_PROGRESS, "Task B description",
                LocalDateTime.of(2022, 6, 2, 15, 15), 20);
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        taskB = TasksHelper.replaceTaskId(taskB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskB);

        Epic epicA = new Epic("Epic A", "Epic A description");
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);

        Subtask subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description",
                LocalDateTime.of(2022, 6, 3, 10, 0), 15, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        Subtask subtaskB = new Subtask(taskManager.getNextTaskId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description",
                null, 15, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskB);
        Subtask subtaskC = new Subtask(taskManager.getNextTaskId(), "Subtask C", TaskStatus.DONE,
                "Subtask C description",
                LocalDateTime.of(2022, 6, 3, 15, 0), 15, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskC);

        taskManager.getTask(taskA.getId());
        taskManager.getTask(taskB.getId());
        taskManager.getEpic(epicA.getId());
        taskManager.getSubtask(subtaskB.getId());

        newTaskManager.load();
        System.out.println(newTaskManager.getTasks());
        System.out.println(newTaskManager.getEpics());
        System.out.println(newTaskManager.getSubtasks());
        System.out.println(newTaskManager.getHistory());

        server.stop();
    }
}