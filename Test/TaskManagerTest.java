import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.tasktracker.manager.TaskManager;
import ru.yandex.practicum.tasktracker.manager.TasksHelper;
import ru.yandex.practicum.tasktracker.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Task taskA;
    protected Task taskB;
    protected Task taskC;
    protected Epic epicA;
    protected Epic epicB;
    protected Subtask subtaskA;
    protected Subtask subtaskB;
    protected Subtask subtaskC;
    protected Subtask subtaskD;
    protected List<Task> manualOrderedTasksList;


    @BeforeEach
    void init() {
        taskA = new Task("Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.of(2022, 6, 1, 12, 45), 15);
        taskB = new Task("Task B", TaskStatus.IN_PROGRESS, "Task B description",
                LocalDateTime.of(2022, 6, 2, 15, 15), 20);
        epicA = new Epic("Epic A", "Epic A description");
        epicB = new Epic("Epic B", "Epic B description");
        subtaskA = null;
        subtaskB = null;
        subtaskC = null;
        subtaskD = null;
    }

    @Test
    @DisplayName("Тест на стандартное добавление задачи в менеджер")
    void addNewTaskTest() {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);

        final Task savedTask = taskManager.getTask(taskA.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(taskA, savedTask, "Задача не соответствует возвращённой по id.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Список задач не получен.");
        assertEquals(1, tasks.size(), "Неверное количество задач в списке задач.");
        assertEquals(taskA, tasks.get(0), "Задача не соответствует сохранённой в списке задач.");
    }

    @Test
    @DisplayName("Тест на стандартное добавление эпика в менеджер")
    void addNewEpicTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);

        final Epic savedEpic = taskManager.getEpic(epicA.getId());

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epicA, savedEpic, "Эпик не соответствует возвращённому по id.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Список эпиков не получен.");
        assertEquals(1, epics.size(), "Неверное количество эпиков в списке эпиков.");
        assertEquals(epicA, epics.get(0), "Задача не соответствует сохранённому в списке эпиков.");
    }

    @Test
    @DisplayName("Тест на стандартное добавление подзадачи в менеджер")
    void addNewSubtaskTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask", TaskStatus.NEW,
                "Subtask description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);

        final Subtask savedSubtask = taskManager.getSubtask(subtaskA.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtaskA, savedSubtask, "Подзадача не соответствует возвращённой по id.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Список эпиков не получен.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач в списке подзадач.");
        assertEquals(subtaskA, subtasks.get(0),
                "Подзадача не соответствует сохранённой в списке подзадач");

        final Set<Integer> epicSubtasks = epicA.getSubtasksMap().keySet();
        assertNotNull(epicSubtasks, "Набор id подзадач эпика не возвращается");
        assertEquals(1, epicSubtasks.size(), "Неверное количество id подзадач в эпике.");
        assertTrue(epicSubtasks.contains(subtaskA.getId()), "id подзадачи не добавлен к эпику.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(subtask) должен прерываться, возвращая 0, если добавляемая подзадача " +
            "относится к null-эпику")
    void addSubtaskDoesNotExecuteIfSubtasksEpicIsNullTest() {
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask", TaskStatus.NEW,
                "Subtask description", LocalDateTime.now(), 30, 0);

        assertEquals(0, taskManager.addTaskOfAnyType(subtaskA),
                "При добавлении задачи с null-эпиком метод возвращает не 0");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(subtask) должен прерываться, возвращая 0, если добавляемая подзадача " +
            "относится к ещё не добавленному эпику")
    void addSubtaskDoesNotExecuteIfSubtasksEpicDoesNotContainsInEpicsTest() {
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask", TaskStatus.NEW,
                "Subtask description", LocalDateTime.now(), 30, epicA.getId());

        assertEquals(0, taskManager.addTaskOfAnyType(subtaskA),
                "При добавлении задачи с ещё не добавленным эпиком метод возвращает не 0");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков не пустой.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(null) должен прерываться, возвращая 0, если добавляемая задач null")
    void addSTaskDoesNotExecuteIfTaskIsNullTest() {
        assertEquals(0, taskManager.addTaskOfAnyType(null),
                "При добавлении null в качестве задачи метод возвращает не 0 в качестве идентификатора");
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач не пустой.");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков не пустой.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(task) должен прерываться, возвращая 0, если добавляется задача с отрицательным id")
    void addSTaskDoesNotExecuteIfTaskIdIsNegativeTest() {
        taskA = new Task(-17, "Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.now(), 30);
        assertEquals(0, taskManager.addTaskOfAnyType(taskA),
                "При добавлении задачи с отрицательным id метод возвращает не 0 в качестве идентификатора");
        assertTrue(taskManager.getTasks().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(task) должен прерываться, возвращая 0, если добавляется задача с уже занятым id")
    void addSTaskDoesNotExecuteIfTaskIdIsOccupiedTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        taskA = new Task(epicA.getId(), "Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.now(), 30);
        assertEquals(0, taskManager.addTaskOfAnyType(taskA),
                "При добавлении задачи с уже занятым id метод возвращает не 0 в качестве идентификатора");
        assertTrue(taskManager.getTasks().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(task) должен увеличивать счётчик задач на 1 от внеочередного значения id")
    void addTaskIncrementsNextIdCounterTest() {
        taskA = new Task(1000000, "Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.now(), 30);
        taskManager.addTaskOfAnyType(taskA);
        assertEquals(1000001, taskManager.getNextTaskId(),
                "При добавлении задачи с внеочередным id = n счётчик id не стал n + 1");
        assertEquals(1, taskManager.getTasks().size(), "Список пуст.");
    }

    @Test
    @DisplayName("getTasks() должен возвращать список из двух стандартно добавленных задач")
    void getTasksTest() {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        taskB = TasksHelper.replaceTaskId(taskB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskB);

        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Список задач не получен.");
        assertEquals(2, tasks.size(), "Неверное количество задач в списке задач.");
        assertEquals(taskA, tasks.get(0), "Задача не соответствует сохранённой в списке задач.");
        assertEquals(taskB, tasks.get(1), "Задача не соответствует сохранённой в списке задач.");
    }

    @Test
    @DisplayName("getTasks() должен возвращать пустой список, если задачи не добавлены")
    void getTasksReturnsEmptyListIfThereAreNoTasks() {
        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Вместо пустого списка — null.");
        assertTrue(tasks.isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getEpics() должен возвращать список из двух стандартно добавленных эпиков")
    void getEpicsTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        epicB = TasksHelper.replaceTaskId(epicB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicB);

        List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Список эпиков не получен.");
        assertEquals(2, epics.size(), "Неверное количество эпиков в списке эпиков.");
        assertEquals(epicA, epics.get(0), "Эпик не соответствует сохранённому в списке эпиков.");
        assertEquals(epicB, epics.get(1), "Эпик не соответствует сохранённому в списке эпиков.");
    }

    @Test
    @DisplayName("getEpics() должен возвращать пустой список, если эпики не добавлены")
    void getEpicsReturnsEmptyListIfThereAreNoEpicsTest() {
        List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Вместо пустого списка — null.");
        assertTrue(epics.isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getSubtasks() должен возвращать список из трёх стандартно добавленных подзадач")
    void getSubtasksTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        epicB = TasksHelper.replaceTaskId(epicB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicB);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", null, 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(taskManager.getNextTaskId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", null, 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = new Subtask(taskManager.getNextTaskId(), "Subtask C", TaskStatus.DONE,
                "Subtask C description", null, 30, epicB.getId());
        taskManager.addTaskOfAnyType(subtaskC);

        List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Список подзадач не получен.");
        assertEquals(3, subtasks.size(), "Неверное количество подзадач в списке подзадач.");
        assertEquals(subtaskA, subtasks.get(0), "Подзадача не соответствует сохранённой в списке задач.");
        assertEquals(subtaskB, subtasks.get(1), "Подзадача не соответствует сохранённой в списке задач.");
        assertEquals(subtaskC, subtasks.get(2), "Подзадача не соответствует сохранённой в списке задач.");
    }

    @Test
    @DisplayName("getSubtasks() должен возвращать пустой список, если подзадачи не добавлены")
    void getSubtasksReturnsEmptyListIfThereAreNoSubtasksTest() {
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Вместо пустого списка — null.");
        assertTrue(subtasks.isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getTasks() должен возвращать пустой список, если все задачи удалены")
    void getTasksReturnsEmptyListAfterRemovingAllTasksTest() {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        taskB = TasksHelper.replaceTaskId(taskB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskB);

        taskManager.removeAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getEpics() должен возвращать пустой список, если все эпики удалены")
    void getEpicsReturnsEmptyListAfterRemovingAllEpicsTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        epicB = TasksHelper.replaceTaskId(epicB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicB);

        taskManager.removeAllEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getSubtasks() должен возвращать пустой список, если все подзадачи удалены")
    void getSubtasksReturnsEmptyListAfterRemovingAllSubtasksTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);

        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", null, 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(taskManager.getNextTaskId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", null, 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskB);

        taskManager.removeAllSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty(), "Список не пустой.");
    }

    @DisplayName("getTask(id) должен возвращать null, если id не существует")
    @ParameterizedTest(name = "{index} getTask({0}) должен возвращать null")
    @MethodSource("nonExistentIdStreamForEmptyMap")
    void getTaskReturnsNullIfIdDoesNotExistTest(int id) {
        assertNull(taskManager.getTask(id), "Результат не null при несуществующем id.");
    }

    @DisplayName("getEpic(id) должен возвращать null, если id не существует")
    @ParameterizedTest(name = "{index} getEpic({0}) должен возвращать null")
    @MethodSource("nonExistentIdStreamForEmptyMap")
    void getEpicReturnsNullIfIdDoesNotExistTest(int id) {
        assertNull(taskManager.getEpic(id), "Результат не null при несуществующем id.");
    }

    @DisplayName("getSubtask(id) должен возвращать null, если id не существует")
    @ParameterizedTest(name = "{index} getSubtask({0}) должен возвращать null")
    @MethodSource("nonExistentIdStreamForEmptyMap")
    void getSubtaskReturnsNullIfIdDoesNotExistTest(int id) {
        assertNull(taskManager.getSubtask(id), "Результат не null при несуществующем id.");
    }

    @Test
    @DisplayName("Тест на стандартную замену задачи")
    void replaceTaskTest() {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        taskB = TasksHelper.replaceTaskId(taskB, taskA.getId());
        taskManager.addTaskOfAnyType(taskB);

        assertTrue(taskManager.replaceTask(taskB), "Метод возвращает не true при стандартной замене задачи.");
        assertEquals(taskB, taskManager.getTask(taskA.getId()), "Заменяющая задача не записалась.");
    }

    @Test
    @DisplayName("replaceTask(null) должен возвращать false")
    void replaceTaskReturnsFalseIfTaskIsNullTest() {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        assertFalse(taskManager.replaceTask(null), "Метод возвращает не false при аргументе null.");
    }

    @DisplayName("replaceTask(task) должен возвращать false, если индекс задачи не существует в менеджере")
    @ParameterizedTest(name = "{index} replaceTask({0}) должен возвращать false")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void replaceTaskReturnsFalseIfTaskIdDoesNotExistTest(int id) {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        taskB = TasksHelper.replaceTaskId(taskB, id);

        assertFalse(taskManager.replaceTask(taskB),
                "Метод возвращает не false при несуществующем id у заменяющей задачи.");
    }

    @Test
    @DisplayName("Тест на стандартную замену эпика")
    void replaceEpicTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        epicB = TasksHelper.replaceTaskId(epicB, epicA.getId());
        taskManager.addTaskOfAnyType(epicB);

        assertTrue(taskManager.replaceEpic(epicB), "Метод возвращает не true при стандартной замене эпика.");
        assertEquals(epicB, taskManager.getEpic(epicA.getId()), "Эпики не совпадают.");
    }

    @Test
    @DisplayName("replaceEpic(null) должен возвращать false")
    void replaceEpicReturnsFalseIfEpicIsNullTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        assertFalse(taskManager.replaceEpic(null), "Метод возвращает не false при аргументе null.");
    }

    @DisplayName("replaceEpic(epic) должен возвращать false, если индекс эпика не существует в менеджере")
    @ParameterizedTest(name = "{index} replaceEpic({0}) должен возвращать false")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void replaceEpicReturnsFalseIfTEpicIdDoesNotExistTest(int id) {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        epicB = TasksHelper.replaceTaskId(epicB, id);

        assertFalse(taskManager.replaceEpic(epicB),
                "Метод возвращает не false при несуществующем id у заменяющего эпика.");
    }

    @Test
    @DisplayName("Тест на стандартную замену подзадачи")
    void replaceSubtaskTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(subtaskA.getId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", null, 15, epicA.getId());

        assertTrue(taskManager.replaceSubtask(subtaskB),
                "Метод возвращает не true при стандартной замене подзадачи.");
        assertEquals(subtaskB, taskManager.getSubtask(subtaskA.getId()), "Подзадачи не совпадают.");
    }

    @Test
    @DisplayName("replaceSubtask(subtask) должен возвращать false, если эпик подзадачи не содержит эту подзадачу")
    void replaceSubtaskReturnsFalseIfSubtasksEpicDoesNotContainsThisSubtaskTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        epicB = TasksHelper.replaceTaskId(epicB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicB);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(subtaskA.getId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", null, 15, epicB.getId());

        assertFalse(taskManager.replaceSubtask(subtaskB),
                "Метод возвращает не false при неправильном эпике в заменяющей подзадаче.");
    }

    @Test
    @DisplayName("replaceSubtask(subtask) должен возвращать false, если эпик заменяющей подзадачи - null")
    void replaceSubtaskReturnsFalseIfSubtasksEpicIsNullTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(subtaskA.getId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", null, 15, 0);

        assertFalse(taskManager.replaceSubtask(subtaskB),
                "Метод возвращает не false при null-эпике в заменяющей подзадаче.");
    }

    @Test
    @DisplayName("replaceSubtask(null) должен возвращать false")
    void replaceSubtaskReturnsFalseIfSubtaskIsNullTest() {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        assertFalse(taskManager.replaceSubtask(null), "Метод возвращает не false при аргументе null.");
    }

    @DisplayName("replaceSubtask(subtask) должен возвращать false, если индекс подзадачи не существует в менеджере")
    @ParameterizedTest(name = "{index} replaceSubtask({0}) должен возвращать false")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void replaceSubtaskReturnsFalseIfSubtasksIdDoesNotExistTest(int id) {
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(id, "Subtask B", TaskStatus.IN_PROGRESS, "Subtask B description",
                null, 15, epicA.getId());

        assertFalse(taskManager.replaceSubtask(subtaskB),
                "Метод возвращает не false при несуществующем id у заменяющей подзадачи.");
    }

    @Test
    @DisplayName("Тест на стандартное удаление задач, эпиков и подзадач")
    void removeTaskTest() {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(taskManager.getNextTaskId(), "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", null, 15, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskB);

        taskManager.removeTaskOfAnyTypeById(taskA.getId());
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач не пуст.");
        taskManager.removeTaskOfAnyTypeById(subtaskA.getId());
        assertEquals(taskManager.getSubtasks().size(), 1,
                "Список подзадач после удаления подзадачи сократился не на 1 элемент.");
        taskManager.removeTaskOfAnyTypeById(epicA.getId());
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков не пуст.");
        assertTrue(taskManager.getSubtasks().isEmpty(),
                "Список подзадач не пуст после удаления эпика, содержавшего подзадачу.");
    }

    @DisplayName("removeTask(id) не должен ничего удалять, если индекс не существует в менеджере")
    @ParameterizedTest(name = "{index} removeTaskOfAnyTypeById({0}) не должен удалять задачи")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void removeTaskDoNotRemoveAnythingIfRemovingIdDoesNotExistTest(int id) {
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);
        subtaskA = new Subtask(taskManager.getNextTaskId(), "Subtask A", TaskStatus.NEW,
                "Subtask A description", LocalDateTime.now(), 30, epicA.getId());
        taskManager.addTaskOfAnyType(subtaskA);

        taskManager.removeTaskOfAnyTypeById(id);
        assertFalse(taskManager.getTasks().isEmpty(), "Список задач пуст.");
        assertFalse(taskManager.getEpics().isEmpty(), "Список эпиков пуст.");
        assertFalse(taskManager.getSubtasks().isEmpty(), "Список подзадач пуст.");
    }

    @Test
    @DisplayName("getHistory() не должен возвращать null")
    void shouldDoNotGetNullInsteadOfHistory() {

        assertNotNull(taskManager.getHistory(), "null вместо списка.");
    }

    @Test
    @DisplayName("Тест на получение сортированного по времени списка приоритетов")
    void prioritizedTasksOrderTest() {
        mixingAddTasksAndSubtasksWhichNamedInOrderByTime();

        final List<Task> manualOrderedTasksList = Arrays.asList(subtaskA, taskA, subtaskB, taskB, subtaskC);
        final List<Task> prioritizedTasksList = taskManager.getPrioritizedTasks();

        assertEquals(manualOrderedTasksList.size(), prioritizedTasksList.size(),
                "В список приоритетов входят не все задачи, которые должны.");
        compareTasksLists(manualOrderedTasksList, prioritizedTasksList);
    }

    @Test
    @DisplayName("Тест на изменение списка приоритетов после удаления задачи")
    void prioritizedTasksChangingAfterRemovingTest() {
        mixingAddTasksAndSubtasksWhichNamedInOrderByTime();

        taskManager.removeTaskOfAnyTypeById(subtaskA.getId());
        manualOrderedTasksList.remove(0);

        final List<Task> prioritizedTasksList = taskManager.getPrioritizedTasks();

        assertEquals(manualOrderedTasksList.size(), prioritizedTasksList.size(),
                "В список приоритетов входят не все задачи, которые должны.");
        compareTasksLists(manualOrderedTasksList, prioritizedTasksList);
    }

    @Test
    @DisplayName("Тест на изменение списка приоритетов после замены задачи")
    void prioritizedTasksChangingAfterReplacingTest() {
        mixingAddTasksAndSubtasksWhichNamedInOrderByTime();

        // замена самой приоритетной задачи на задачу без приоритета
        // задача должна переместиться в самый конец, поскольку у неё также был последний id
        subtaskD = new Subtask(subtaskA.getId(), "Subtask D", TaskStatus.NEW,
                "Subtask B description", null, 0, epicA.getId());

        taskManager.replaceSubtask(subtaskD);
        manualOrderedTasksList.remove(0);
        manualOrderedTasksList.add(subtaskD);

        final List<Task> prioritizedTasksList = taskManager.getPrioritizedTasks();

        assertEquals(manualOrderedTasksList.size(), prioritizedTasksList.size(),
                "В список приоритетов входят не все задачи, которые должны.");
        compareTasksLists(manualOrderedTasksList, prioritizedTasksList);
    }

    @DisplayName("Тест на добавление задач перекрывающихся / непересекающихся по времени")
    @ParameterizedTest(name = "{index} проверка интервала с началом {0} длительностью {1}")
    @MethodSource("timeOverlappingStreamForAddingTasks")
    void addingTimeOverlappingTaskTest(LocalDateTime startTime, int duration, boolean isOverlapping) {
        mixingAddTasksAndSubtasksWhichNamedInOrderByTime();
        int tasksOldSize = taskManager.getTasks().size();

        taskC = new Task(taskManager.getNextTaskId(), "Task C", TaskStatus.NEW,
                "Task B description", startTime, duration);

        assertEquals(isOverlapping, (taskManager.addTaskOfAnyType(taskC) == 0),
                "Результат добавления задачи не соответствует ожидаемому.");
        assertEquals(isOverlapping, (tasksOldSize == taskManager.getTasks().size()),
                "Результат добавления задачи не соответствует ожидаемому.");
    }

    @DisplayName("Тест на добавление задач перекрывающихся / непересекающихся по времени")
    @ParameterizedTest(name = "{index} проверка интервала с началом {0} длительностью {1}")
    @MethodSource("timeOverlappingStreamForAddingTasks")
    void addingTimeOverlappingSubtaskTest(LocalDateTime startTime, int duration, boolean isOverlapping) {
        mixingAddTasksAndSubtasksWhichNamedInOrderByTime();
        int subtasksOldSize = taskManager.getSubtasks().size();

        subtaskD = new Subtask(taskManager.getNextTaskId(), "Subtask D", TaskStatus.NEW,
                "Subtask B description",
                startTime, duration, epicA.getId());

        assertEquals(isOverlapping, (taskManager.addTaskOfAnyType(subtaskD) == 0),
                "Результат добавления подзадачи не соответствует ожидаемому.");
        assertEquals(isOverlapping, (subtasksOldSize == taskManager.getSubtasks().size()),
                "Результат добавления подзадачи не соответствует ожидаемому.");
    }

    @DisplayName("Тест на замену задач перекрывающихся / непересекающихся по времени")
    @ParameterizedTest(name = "{index} проверка интервала с началом {0} длительностью {1}")
    @MethodSource("timeOverlappingStreamForAddingTasks")
    void replacingTimeOverlappingTaskTest(LocalDateTime startTime, int duration, boolean isOverlapping) {
        mixingAddTasksAndSubtasksWhichNamedInOrderByTime();

        taskC = new Task(taskB.getId(), "Task C", TaskStatus.NEW,
                "Task B description", startTime, duration);

        assertEquals(isOverlapping, !taskManager.replaceTask(taskC),
                "Результат замены задачи не соответствует ожидаемому.");
        assertEquals(isOverlapping, !taskC.equals(taskManager.getTask(taskC.getId())),
                "Результат замены задачи не соответствует ожидаемому.");
    }

    @DisplayName("Тест на замену подзадач перекрывающихся / непересекающихся по времени")
    @ParameterizedTest(name = "{index} проверка интервала с началом {0} длительностью {1}")
    @MethodSource("timeOverlappingStreamForAddingTasks")
    void replacingTimeOverlappingSubtaskTest(LocalDateTime startTime, int duration, boolean isOverlapping) {
        mixingAddTasksAndSubtasksWhichNamedInOrderByTime();

        subtaskD = new Subtask(subtaskC.getId(), "Subtask D", TaskStatus.NEW,
                "Subtask B description",
                startTime, duration, epicA.getId());

        assertEquals(isOverlapping, !taskManager.replaceSubtask(subtaskD),
                "Результат замены подзадачи не соответствует ожидаемому.");
        assertEquals(isOverlapping, !subtaskD.equals(taskManager.getSubtask(subtaskD.getId())),
                "Результат замены подзадачи не соответствует ожидаемому.");
    }

    protected <V extends Task> void compareTasksLists(List<V> list1, List<V> list2) {
        assertEquals(list1.size(), list2.size(), "Размеры списка задач не совпадают.");
        for (int i = 0; i < list1.size(); i++) {
            assertEquals(list1.get(i), list2.get(i), "Элементы списка задач не совпадают.");
        }
    }

    private static Stream<Arguments> nonExistentIdStreamForEmptyMap() {
        return Stream.of(-1000000, -1, 0, 1, 1000000).map(Arguments::of);
    }

    private static Stream<Arguments> nonExistentIdStreamForMapWithFewItems() {
        return Stream.of(-1000000, -1, 0, 11, 1000000).map(Arguments::of);
    }

    private void mixingAddTasksAndSubtasksWhichNamedInOrderByTime() {
        epicA = new Epic("Epic A", "Epic A description");
        epicA = TasksHelper.replaceTaskId(epicA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(epicA);

        // Задачам и подзадачам присвоены значения в порядке возрастания даты начала
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description",
                LocalDateTime.of(2022, 6, 1, 10, 30), 30, epicA.getId());

        taskA = new Task("Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.of(2022, 6, 1, 15, 15), 15);

        subtaskB = new Subtask("Subtask B", TaskStatus.NEW, "Subtask B description",
                LocalDateTime.of(2022, 6, 5, 18, 0), 120, epicA.getId());

        taskB = new Task("Task B", TaskStatus.NEW, "Task B description",
                null, 20);

        subtaskC = new Subtask("Subtask B", TaskStatus.NEW, "Subtask B description",
                null, 0, epicA.getId());

        // Задачам и подзадачам записываются в менеджер в перемешанном порядке

        taskB = TasksHelper.replaceTaskId(taskB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskB);
        subtaskB = TasksHelper.replaceTaskId(subtaskB, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = TasksHelper.replaceTaskId(subtaskC, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(subtaskC);
        taskA = TasksHelper.replaceTaskId(taskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(taskA);
        subtaskA = TasksHelper.replaceTaskId(subtaskA, taskManager.getNextTaskId());
        taskManager.addTaskOfAnyType(subtaskA);

        manualOrderedTasksList = new ArrayList<>();
        manualOrderedTasksList.add(subtaskA);
        manualOrderedTasksList.add(taskA);
        manualOrderedTasksList.add(subtaskB);
        manualOrderedTasksList.add(taskB);
        manualOrderedTasksList.add(subtaskC);
    }

    private static Stream<Arguments> timeOverlappingStreamForAddingTasks() {
        return Stream.of(
                // Пересечение с интервалом самой ранней задачи 2022.06.01 10:30 - 11:00
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 15), 30, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 40), 5, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 45), 30, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 15), 60, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 30), 30, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 30), 1, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 59), 1, true),
                // Пересечение с интервалом серединной задачи 2022.06.01 15:15 - 15:30
                Arguments.of(LocalDateTime.of(2022, 6, 1, 15, 10), 15, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 15, 20), 5, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 15, 20), 15, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 15, 0), 45, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 15, 15), 15, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 15, 15), 1, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 15, 29), 1, true),
                // Пересечение с интервалом самой поздней задачи 2022.06.05 18:00 - 20:00
                Arguments.of(LocalDateTime.of(2022, 6, 5, 17, 0), 120, true),
                Arguments.of(LocalDateTime.of(2022, 6, 5, 19, 0), 60, true),
                Arguments.of(LocalDateTime.of(2022, 6, 5, 19, 0), 120, true),
                Arguments.of(LocalDateTime.of(2022, 6, 5, 17, 0), 240, true),
                Arguments.of(LocalDateTime.of(2022, 6, 5, 18, 0), 120, true),
                Arguments.of(LocalDateTime.of(2022, 6, 5, 18, 0), 1, true),
                Arguments.of(LocalDateTime.of(2022, 6, 5, 19, 59), 1, true),
                // Пересечение с интервалом всех задач 2022.06.01 10:30 - 2022.06.05 20:00
                Arguments.of(LocalDateTime.of(2022, 1, 1, 0, 0), 526000, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 30), 6330, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 29), 6332, true),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 31), 6328, true),
                // Нет пересечения - до самой ранней задачи
                Arguments.of(LocalDateTime.of(2022, 6, 1, 9, 0), 60, false),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 10, 29), 1, false),
                // Нет пересечения - после самой поздней задачи
                Arguments.of(LocalDateTime.of(2022, 6, 5, 21, 0), 60, false),
                Arguments.of(LocalDateTime.of(2022, 6, 5, 20, 0), 1, false),
                // Нет пересечения - между задачами
                Arguments.of(LocalDateTime.of(2022, 6, 1, 12, 0), 120, false),
                Arguments.of(LocalDateTime.of(2022, 6, 1, 11, 0), 255, false),
                // Нет пересечения - время задачи null
                Arguments.of(null, 0, false),
                Arguments.of(null, 100, false)
        );
    }

}