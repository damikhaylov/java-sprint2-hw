import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tasktracker.manager.HTTPTaskManager;
import ru.yandex.practicum.tasktracker.server.KVServer;
import ru.yandex.practicum.tasktracker.manager.TasksHelper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HTTPTaskManagerTest extends TaskManagerTest<HTTPTaskManager> {
    private final String URL = "http://localhost:8078";
    private KVServer server;
    private HTTPTaskManager newTaskManager;

    @BeforeEach
    @Override
    void init() {
        try {
            server = new KVServer();
            server.start();
        } catch (IOException e) {
            System.out.println("Ошибка при запуске KV-сервера.");
            e.printStackTrace();
        }
        taskManager = new HTTPTaskManager(URL);
        super.init();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("Тест на запись-считывание стандартного набора задач и истории")
    void writeAndReadBakDataFileStandardTest() {
        Add2TasksAndEpicWith3Subtasks();
        // Просмотр задач для формирования истории
        taskManager.getTask(taskA.getId());
        taskManager.getTask(taskB.getId());
        taskManager.getEpic(epicA.getId());
        taskManager.getSubtask(subtaskB.getId());

        newTaskManager = new HTTPTaskManager(URL);
        newTaskManager.load();

        compareManagersLists(taskManager, newTaskManager);
    }

    @Test
    @DisplayName("Тест на запись-считывание стандартного набора задач без истории")
    void writeAndReadBakDataFileWithoutHistoryTest() {
        Add2TasksAndEpicWith3Subtasks();
        newTaskManager = new HTTPTaskManager(URL);
        newTaskManager.load();
        compareManagersLists(taskManager, newTaskManager);
        assertTrue(newTaskManager.getHistory().isEmpty(), "Список истории не пустой.");
    }

    @Test
    @DisplayName("Тест на запись-считывание эпика без подзадач")
    void writeAndReadBakDataFileAloneEpicTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        newTaskManager = new HTTPTaskManager(URL);
        newTaskManager.load();
        compareManagersLists(taskManager, newTaskManager);
        assertEquals(1, newTaskManager.getEpics().size(), "Размер списка эпиков не равен 1");
        assertTrue(newTaskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("Тест на запись-считывание пустого набора задач")
    void writeAndReadEmptyBakDataFileTest() {
        taskManager.addTaskOfAnyType(taskA);
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        newTaskManager = new HTTPTaskManager(URL);
        newTaskManager.load();
        compareManagersLists(taskManager, newTaskManager);
        noDataRecordedToManagerCheck(newTaskManager);
    }
}
