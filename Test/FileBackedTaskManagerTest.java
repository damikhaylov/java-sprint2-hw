import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tasktracker.manager.FileBackedTaskManager;
import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;
import ru.yandex.practicum.tasktracker.model.TaskStatus;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private final String FILENAME = "tasks.csv";
    private int taskAId;
    private int taskBId;
    private int epicAId;
    private int epicBId;
    private int subtaskAId;
    private int subtaskBId;
    private int subtaskCId;
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
        taskManager.getTask(taskAId);
        taskManager.getTask(taskBId);
        taskManager.getEpic(epicAId);
        taskManager.getSubtask(subtaskAId);

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
        newTaskManager = new FileBackedTaskManager(new File(FILENAME), true);
        compareManagersLists();
        assertTrue(newTaskManager.getTasks().isEmpty(), "Список задач не пустой.");
        assertTrue(newTaskManager.getEpics().isEmpty(), "Список эпиков не пустой.");
        assertTrue(newTaskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
        assertTrue(newTaskManager.getHistory().isEmpty(), "Список истории не пустой.");
    }

    public void Add2TasksAndEpicWith3Subtasks() {
        Task task; // Объект для формирования задачи и передачи в методы TaskManager
        Epic epic; // Объект для формирования эпика и передачи в методы TaskManager
        Subtask subtask; // Объект для формирования подзадачи и передачи в методы TaskManager

        // Создание двух задач
        taskAId = taskManager.addTaskOfAnyType(taskA);
        task = new Task("TaskB", TaskStatus.NEW, "Task B description");
        taskBId = taskManager.addTaskOfAnyType(taskB);

        // Создание эпика с тремя подзадачами
        epic = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        epicAId = epic.getId();

        subtask = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epic);
        subtaskAId = taskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask("Subtask B", TaskStatus.IN_PROGRESS, "Subtask B description", epic);
        subtaskBId = taskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask("Subtask C", TaskStatus.DONE, "Subtask C description", epic);
        subtaskCId = taskManager.addTaskOfAnyType(subtask);
    }

    private void compareManagersLists() {
        (new taskListsComparation<Task>(taskManager.getTasks(), newTaskManager.getTasks())).compare();
        (new taskListsComparation<Epic>(taskManager.getEpics(), newTaskManager.getEpics())).compare();
        (new taskListsComparation<Subtask>(taskManager.getSubtasks(), newTaskManager.getSubtasks())).compare();
        (new taskListsComparation<Task>(taskManager.getHistory(), newTaskManager.getHistory())).compare();
    }
}


class taskListsComparation<T extends Task> {
    private final List<T> list1;
    private final List<T> list2;

    taskListsComparation(List<T> list1, List<T> list2) {
        this.list1 = list1;
        this.list2 = list2;
    }

    public void compare() {
        assertEquals(list1.size(), list2.size(), "Размеры списка задач не совпадают.");
        for (int i = 0; i < list1.size(); i++) {
            assertEquals(list2.get(i), list1.get(i), "Элементы списка задач не совпадают.");
        }
    }
}
