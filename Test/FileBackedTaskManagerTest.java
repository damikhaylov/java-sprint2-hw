import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tasktracker.exeption.ManagerSaveException;
import ru.yandex.practicum.tasktracker.manager.FileBackedTaskManager;
import ru.yandex.practicum.tasktracker.manager.TasksHelper;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private final String FILENAME = "tasks.csv";
    private FileBackedTaskManager newTaskManager;

    @BeforeEach
    @Override
    void init() {
        taskManager = new FileBackedTaskManager(FILENAME, false);
        super.init();
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

        newTaskManager = new FileBackedTaskManager(FILENAME, true);

        compareManagersLists(taskManager, newTaskManager);
    }

    @Test
    @DisplayName("Тест на запись-считывание стандартного набора задач без истории")
    void writeAndReadBakDataFileWithoutHistoryTest() {
        Add2TasksAndEpicWith3Subtasks();
        newTaskManager = new FileBackedTaskManager(FILENAME, true);
        compareManagersLists(taskManager, newTaskManager);
        assertTrue(newTaskManager.getHistory().isEmpty(), "Список истории не пустой.");
    }

    @Test
    @DisplayName("Тест на запись-считывание эпика без подзадач")
    void writeAndReadBakDataFileAloneEpicTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        newTaskManager = new FileBackedTaskManager(FILENAME, true);
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
        newTaskManager = new FileBackedTaskManager(FILENAME, true);
        compareManagersLists(taskManager, newTaskManager);
        noDataRecordedToManagerCheck(newTaskManager);
    }

    @Test
    @DisplayName("Тест на чтение из недоступного файла")
    void readFromBadFileTest() {
        newTaskManager = new FileBackedTaskManager("*?/", true);
        noDataRecordedToManagerCheck(newTaskManager);
    }

    @Test
    @DisplayName("Тест на запись в недоступный файл")
    void writeToBadFileTest() {
        taskManager = new FileBackedTaskManager("*?/", false);
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        final ManagerSaveException exception = assertThrows(
                ManagerSaveException.class,
                () -> taskManager.addTaskOfAnyType(taskA)
        );
        assertTrue(exception.getMessage().contains("Ошибка записи в файл"));
    }

    @Test
    @DisplayName("Тест на чтение из файла одной строкой заголовка или вообще без строк")
    void readFromOnlyHeaderOrEmptyFileTest() {
        newTaskManager = new FileBackedTaskManager("only_header.csv", true);
        noDataRecordedToManagerCheck(newTaskManager);
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверным количеством csv полей")
    void readFromWrongFieldsCountFileTest() {
        newTaskManager = new FileBackedTaskManager("wrong_fields_count.csv", true);
        noDataRecordedToManagerCheck(newTaskManager);
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверным форматом данных в полях")
    void readFromWrongDataFormatFileTest() {
        newTaskManager = new FileBackedTaskManager("wrong_data_format.csv", true);
        noDataRecordedToManagerCheck(newTaskManager);
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверной ссылкой на эпик в записи подзадачи")
    void readFromWrongEpicIdInSubtaskRecordFileTest() {
        newTaskManager = new FileBackedTaskManager("wrong_epic_id.csv", true);
        noDataRecordedToManagerCheck(newTaskManager);
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверным форматом списка истории")
    void readFromWrongHistoryFileTest() {
        newTaskManager = new FileBackedTaskManager("wrong_history.csv", true);
        assertTrue(newTaskManager.getHistory().isEmpty(), "Список истории не пустой.");
    }
}
