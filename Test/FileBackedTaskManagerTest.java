import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tasktracker.exeption.ManagerSaveException;
import ru.yandex.practicum.tasktracker.manager.FileBackedTaskManager;
import ru.yandex.practicum.tasktracker.manager.TasksHelper;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.TaskStatus;

import java.io.File;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private final String FILENAME = "tasks.csv";
    private FileBackedTaskManager newTaskManager;

    @BeforeEach
    @Override
    void init() {
        taskManager = new FileBackedTaskManager(new File(FILENAME), false);
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

        newTaskManager = new FileBackedTaskManager(new File(FILENAME), true);

        compareManagersLists();
    }

    @Test
    @DisplayName("Тест на запись-считывание стандартного набора задач без истории")
    void writeAndReadBakDataFileWithoutHistoryTest() {
        Add2TasksAndEpicWith3Subtasks();
        newTaskManager = new FileBackedTaskManager(new File(FILENAME), true);
        compareManagersLists();
        assertTrue(newTaskManager.getHistory().isEmpty(), "Список истории не пустой.");
    }

    @Test
    @DisplayName("Тест на запись-считывание эпика без подзадач")
    void writeAndReadBakDataFileAloneEpicTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        newTaskManager = new FileBackedTaskManager(new File(FILENAME), true);
        compareManagersLists();
        assertEquals(1, newTaskManager.getEpics().size(), "Размер списка эпиков не равен 1");
        assertTrue(newTaskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("Тест на запись-считывание пустого набора задач")
    void writeAndReadEmptyBakDataFileTest() {
        taskManager.addTaskOfAnyType(taskA);
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        newTaskManager = new FileBackedTaskManager(new File(FILENAME), true);
        compareManagersLists();
        noDataRecordedToManagerCheck();
    }

    @Test
    @DisplayName("Тест на чтение из недоступного файла")
    void readFromBadFileTest() {
        newTaskManager = new FileBackedTaskManager(new File("*?/"), true);
        noDataRecordedToManagerCheck();
    }

    @Test
    @DisplayName("Тест на запись в недоступный файл")
    void writeToBadFileTest() {
        taskManager = new FileBackedTaskManager(new File("*?/"), false);
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
        newTaskManager = new FileBackedTaskManager(new File("only_header.csv"), true);
        noDataRecordedToManagerCheck();
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверным количеством csv полей")
    void readFromWrongFieldsCountFileTest() {
        newTaskManager = new FileBackedTaskManager(new File("wrong_fields_count.csv"), true);
        noDataRecordedToManagerCheck();
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверным форматом данных в полях")
    void readFromWrongDataFormatFileTest() {
        newTaskManager = new FileBackedTaskManager(new File("wrong_data_format.csv"), true);
        noDataRecordedToManagerCheck();
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверной ссылкой на эпик в записи подзадачи")
    void readFromWrongEpicIdInSubtaskRecordFileTest() {
        newTaskManager = new FileBackedTaskManager(new File("wrong_epic_id.csv"), true);
        noDataRecordedToManagerCheck();
    }

    @Test
    @DisplayName("Тест на чтение из файла c неверной ссылкой на эпик в записи подзадачи")
    void readFromWrongHistoryFileTest() {
        newTaskManager = new FileBackedTaskManager(new File("wrong_history.csv"), true);
        assertTrue(newTaskManager.getHistory().isEmpty(), "Список истории не пустой.");
    }

    public void Add2TasksAndEpicWith3Subtasks() {
        // Создание двух задач
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        taskB = TasksHelper.replaceTaskId(taskB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskB);

        // Создание эпика с тремя подзадачами
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);

        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description",
                LocalDateTime.of(2022, 6, 3, 10, 0), 15, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(taskManager.getNextTaskId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description",
                null, 15, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = new Subtask(taskManager.getNextTaskId(), "Subtask C", TaskStatus.DONE,
                "Subtask C description",
                LocalDateTime.of(2022, 6, 3, 15, 0), 15, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskC);
    }

    private void compareManagersLists() {
        compareTasksLists(taskManager.getTasks(), newTaskManager.getTasks());
        compareTasksLists(taskManager.getEpics(), newTaskManager.getEpics());
        compareTasksLists(taskManager.getSubtasks(), newTaskManager.getSubtasks());
        compareTasksLists(taskManager.getHistory(), newTaskManager.getHistory());
        compareTasksLists(taskManager.getPrioritizedTasks(), newTaskManager.getPrioritizedTasks());
    }

    private void noDataRecordedToManagerCheck() {
        assertTrue(newTaskManager.getTasks().isEmpty(), "Список задач не пустой.");
        assertTrue(newTaskManager.getEpics().isEmpty(), "Список эпиков не пустой.");
        assertTrue(newTaskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
        assertTrue(newTaskManager.getHistory().isEmpty(), "Список истории не пустой.");
    }
}
