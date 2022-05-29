import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.tasktracker.manager.TaskManager;
import ru.yandex.practicum.tasktracker.model.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Task taskA;
    protected Task taskB;
    protected Epic epicA;
    protected Epic epicB;
    protected Subtask subtaskA;
    protected Subtask subtaskB;
    protected Subtask subtaskC;


    @BeforeEach
    void init() {
        taskA = new Task("Task A", TaskStatus.NEW, "Task A description");
        taskB = new Task("Task B", TaskStatus.IN_PROGRESS, "Task B description");
        epicA = new Epic("Epic A", "Epic A description");
        epicB = new Epic("Epic B", "Epic B description");
    }


    // TODO: Комментарий для ревью (удалить после спринта 6) - Тесты добавления задачи, эпика, подзадачи реализованы
    //  как в примере ТЗ. Поскольку тест на стандартное поведение методов типа getTask(id) включено в эти тесты,
    //  в дальнейшем тестируются только граничные условия getTask(id). Метод addTaskOfAnyTypeAndReturnTask(task) не
    //  тестируется, поскольку протестирован оборачивающий его метод addTaskOfAnyType(task).
    //  Тестирование изменения статуса эпиков при выполнении методов TaskManager вынесено в отдельный класс
    //  EpicStatusTest

    @Test
    @DisplayName("Тест на стандартное добавление задачи в менеджер")
    void addNewTaskTest() {
        final int taskId = taskManager.addTaskOfAnyType(taskA);
        final Task initialTaskWithActualId = TaskManager.replaceTaskId(taskA, taskId);

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(initialTaskWithActualId, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(initialTaskWithActualId, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    @DisplayName("Тест на стандартное добавление эпика в менеджер")
    void addNewEpicTest() {
        final int epicId = taskManager.addTaskOfAnyType(epicA);
        final Epic initialEpicWithActualId = (Epic) TaskManager.replaceTaskId(epicA, epicId);

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(initialEpicWithActualId, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(initialEpicWithActualId, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    @DisplayName("Тест на стандартное добавление подзадачи в менеджер")
    void addNewSubtaskTest() {
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        subtaskA = new Subtask("Subtask", TaskStatus.NEW, "Subtask description", epicA);
        final int subtaskId = taskManager.addTaskOfAnyType(subtaskA);
        final Subtask initialSubTaskWithActualId = (Subtask) TaskManager.replaceTaskId(subtaskA, subtaskId);

        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(initialSubTaskWithActualId, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(initialSubTaskWithActualId, subtasks.get(0), "Подзадачи не совпадают.");

        final Set<Integer> epicSubtasks = epicA.getSubtasksIdSet();
        assertNotNull(epicSubtasks, "Набор id подзадач эпика не возвращается");
        assertEquals(1, epicSubtasks.size(), "Неверное количество id подзадач в эпике.");
        assertTrue(epicSubtasks.contains(subtaskId), "id подзадачи не добавлен к эпику.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(subtask) должен прерываться, возвращая 0, если добавляемая подзадача " +
            "относится к null-эпику")
    void shouldReturn0IfAddingSubtasksEpicIsNull() {
        subtaskA = new Subtask("Subtask", TaskStatus.NEW, "Subtask description", null);

        assertEquals(0, taskManager.addTaskOfAnyType(subtaskA),
                "При добавлении задачи с null-эпиком метод возвращает не 0");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(subtask) должен прерываться, возвращая 0, если добавляемая подзадача " +
            "относится к ещё не добавленному эпику")
    void shouldReturn0IfAddingSubtasksEpicDoesNotContainsInEpics() {
        subtaskA = new Subtask("Subtask", TaskStatus.NEW, "Subtask description", epicA);

        assertEquals(0, taskManager.addTaskOfAnyType(subtaskA),
                "При добавлении задачи с ещё не добавленным эпиком метод возвращает не 0");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков не пустой.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(subtask) должен прерываться, возвращая 0, если добавляемая подзадача " +
            "относится к эпику, который отличается от сохранённого с его id")
    void shouldReturn0IfAddingSubtasksEpicDoesNotEqualsValueFromEpics() {
        int epicId = taskManager.addTaskOfAnyType(epicA);
        epicB = (Epic) TaskManager.replaceTaskId(epicB, epicId);
        subtaskA = new Subtask("Subtask", TaskStatus.NEW, "Subtask description", epicB);

        assertEquals(0, taskManager.addTaskOfAnyType(subtaskA),
                "При добавлении задачи с эпиком, не совпадающим с сохранённым, метод возвращает не 0");
        assertFalse(taskManager.getEpics().isEmpty(), "Список эпиков пуст.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(null) должен прерываться, возвращая 0")
    void shouldReturn0IfAddingTaskIsNull() {
        assertEquals(0, taskManager.addTaskOfAnyType(null),
                "При добавлении null в качестве задачи метод возвращает не 0 в качестве идентификатора");
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач не пустой.");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков не пустой.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(task) должен прерываться, возвращая 0, если добавляется задача с отрицательным id")
    void shouldReturn0IfAddingTaskWithNegativeId() {
        taskA = new Task(-17, "Task A", TaskStatus.NEW, "Task A description");
        assertEquals(0, taskManager.addTaskOfAnyType(taskA),
                "При добавлении задачи с отрицательным id метод возвращает не 0 в качестве идентификатора");
        assertTrue(taskManager.getTasks().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(task) должен прерываться, возвращая 0, если добавляется задача с уже занятым id")
    void shouldReturn0IfAddingTaskWithOccupiedId() {
        int id = taskManager.addTaskOfAnyType(epicA);
        taskA = new Task(id, "Task A", TaskStatus.NEW, "Task A description");
        assertEquals(0, taskManager.addTaskOfAnyType(taskA),
                "При добавлении задачи с уже занятым id метод возвращает не 0 в качестве идентификатора");
        assertTrue(taskManager.getTasks().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("addTaskOfAnyType(task) должен увеличивать счётчик задач на 1 от внеочередного значения id")
    void shouldIncrementNextIdAfterAddingTaskWithNonOrderedId() {
        taskA = new Task(1000000, "Task A", TaskStatus.NEW, "Task A description");
        taskManager.addTaskOfAnyType(taskA);
        assertEquals(1000001, taskManager.getNextTaskId(),
                "При добавлении задачи с внеочередным id = n счётчик id не стал n + 1");
        assertEquals(1, taskManager.getTasks().size(), "Список пуст.");
    }

    @Test
    @DisplayName("getTasks() должен возвращать список из двух стандартно добавленных задач")
    void shouldGet2ElementsListAfterAdding2Tasks() {
        int taskId = taskManager.addTaskOfAnyType(taskA);
        final Task initialTaskAWithActualId = TaskManager.replaceTaskId(taskA, taskId);
        taskId = taskManager.addTaskOfAnyType(taskB);
        final Task initialTaskBWithActualId = TaskManager.replaceTaskId(taskB, taskId);

        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(initialTaskAWithActualId, tasks.get(0), "Задачи не совпадают.");
        assertEquals(initialTaskBWithActualId, tasks.get(1), "Задачи не совпадают.");
    }

    @Test
    @DisplayName("getTasks() должен возвращать пустой список, если задачи не добавлены")
    void shouldGetEmptyListOfTasks() {
        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Вместо пустого списка — null.");
        assertTrue(tasks.isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getEpics() должен возвращать список из двух стандартно добавленных эпиков")
    void shouldGet2ElementsListAfterAdding2Epics() {
        int epicId = taskManager.addTaskOfAnyType(epicA);
        final Epic initialEpicAWithActualId = (Epic) TaskManager.replaceTaskId(epicA, epicId);
        epicId = taskManager.addTaskOfAnyType(epicB);
        final Epic initialEpicBWithActualId = (Epic) TaskManager.replaceTaskId(epicB, epicId);

        List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(2, epics.size(), "Неверное количество эпиков.");
        assertEquals(initialEpicAWithActualId, epics.get(0), "Эпики не совпадают.");
        assertEquals(initialEpicBWithActualId, epics.get(1), "Эпики не совпадают.");
    }

    @Test
    @DisplayName("getEpics() должен возвращать пустой список, если эпики не добавлены")
    void shouldGetEmptyListOfEpics() {
        List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Вместо пустого списка — null.");
        assertTrue(epics.isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getSubtasks() должен возвращать список из трёх стандартно добавленных подзадач")
    void shouldGet3ElementsListAfterAdding3Subtasks() {
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        epicB = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicB);
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        subtaskB = new Subtask("Subtask B", TaskStatus.IN_PROGRESS, "Subtask B description", epicA);
        subtaskC = new Subtask("Subtask C", TaskStatus.IN_PROGRESS, "Subtask C description", epicB);
        int subtaskId = taskManager.addTaskOfAnyType(subtaskA);
        final Subtask initialSubTaskAWithActualId = (Subtask) TaskManager.replaceTaskId(subtaskA, subtaskId);
        subtaskId = taskManager.addTaskOfAnyType(subtaskB);
        final Subtask initialSubTaskBWithActualId = (Subtask) TaskManager.replaceTaskId(subtaskB, subtaskId);
        subtaskId = taskManager.addTaskOfAnyType(subtaskC);
        final Subtask initialSubTaskCWithActualId = (Subtask) TaskManager.replaceTaskId(subtaskC, subtaskId);


        List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(3, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(initialSubTaskAWithActualId, subtasks.get(0), "Подзадачи не совпадают.");
        assertEquals(initialSubTaskBWithActualId, subtasks.get(1), "Подзадачи не совпадают.");
        assertEquals(initialSubTaskCWithActualId, subtasks.get(2), "Подзадачи не совпадают.");
    }

    @Test
    @DisplayName("getSubtasks() должен возвращать пустой список, если подзадачи не добавлены")
    void shouldGetEmptyListOfSubtasks() {
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Вместо пустого списка — null.");
        assertTrue(subtasks.isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getTasks() должен возвращать пустой список, если все задачи удалены")
    void shouldGetEmptyListAfterRemovingAllTasks() {
        taskManager.addTaskOfAnyType(taskA);
        taskManager.addTaskOfAnyType(taskB);

        taskManager.removeAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getEpics() должен возвращать пустой список, если все эпики удалены")
    void shouldGetEmptyListAfterRemovingAllEpics() {
        taskManager.addTaskOfAnyType(epicA);
        taskManager.addTaskOfAnyType(epicB);

        taskManager.removeAllEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Список не пустой.");
    }

    @Test
    @DisplayName("getSubtasks() должен возвращать пустой список, если все подзадачи удалены")
    void shouldGetEmptyListAfterRemovingAllSubtasks() {
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        subtaskB = new Subtask("Subtask B", TaskStatus.IN_PROGRESS, "Subtask B description", epicA);
        taskManager.addTaskOfAnyType(subtaskA);
        taskManager.addTaskOfAnyType(subtaskB);

        taskManager.removeAllSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty(), "Список не пустой.");
    }

    @DisplayName("getTask(id) должен возвращать null, если id не существует")
    @ParameterizedTest(name = "{index} getTask({0}) должен возвращать null")
    @MethodSource("nonExistentIdStreamForEmptyMap")
    void shouldGetNullIfTaskIdDoesNotExist(int id) {
        assertNull(taskManager.getTask(id), "Результат не null при несуществующем id.");
    }

    @DisplayName("getEpic(id) должен возвращать null, если id не существует")
    @ParameterizedTest(name = "{index} getEpic({0}) должен возвращать null")
    @MethodSource("nonExistentIdStreamForEmptyMap")
    void shouldGetNullIfEpicIdDoesNotExist(int id) {
        assertNull(taskManager.getEpic(id), "Результат не null при несуществующем id.");
    }

    @DisplayName("getSubtask(id) должен возвращать null, если id не существует")
    @ParameterizedTest(name = "{index} getSubtask({0}) должен возвращать null")
    @MethodSource("nonExistentIdStreamForEmptyMap")
    void shouldGetNullIfSubtaskIdDoesNotExist(int id) {
        assertNull(taskManager.getSubtask(id), "Результат не null при несуществующем id.");
    }

    @Test
    @DisplayName("Тест на стандартную замену задачи")
    void replaceTaskTest() {
        int id = taskManager.addTaskOfAnyType(taskA);
        taskB = new Task(id, "Task B", TaskStatus.IN_PROGRESS, "Task B description");

        assertTrue(taskManager.replaceTask(taskB), "Метод возвращает не true при стандартной замене задачи.");
        assertEquals(taskB, taskManager.getTask(id), "Задачи не совпадают.");
    }

    @Test
    @DisplayName("replaceTask(null) должен возвращать false")
    void shouldReturnFalseIfReplacingTaskIsNull() {
        taskManager.addTaskOfAnyType(taskA);
        assertFalse(taskManager.replaceTask(null), "Метод возвращает не false при аргументе null.");
    }

    @DisplayName("replaceTask(task) должен возвращать false, если индекс задачи не существует в менеджере")
    @ParameterizedTest(name = "{index} replaceTask({0}) должен возвращать false")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void shouldReturnFalseIfReplacingTaskIdDoesNotExist(int id) {
        taskManager.addTaskOfAnyType(taskA);
        taskB = new Task(id, "Task B", TaskStatus.IN_PROGRESS, "Task B description");

        assertFalse(taskManager.replaceTask(taskB),
                "Метод возвращает не false при несуществующем id у заменяющей задачи.");
    }

    @Test
    @DisplayName("Тест на стандартную замену эпика")
    void replaceEpicTest() {
        int id = taskManager.addTaskOfAnyType(epicA);
        epicB = new Epic(id, "Epic B", "Epic B description");

        assertTrue(taskManager.replaceEpic(epicB), "Метод возвращает не true при стандартной замене эпика.");
        assertEquals(epicB, taskManager.getEpic(id), "Эпики не совпадают.");
    }

    @Test
    @DisplayName("replaceEpic(null) должен возвращать false")
    void shouldReturnFalseIfReplacingEpicIsNull() {
        taskManager.addTaskOfAnyType(epicA);
        assertFalse(taskManager.replaceEpic(null), "Метод возвращает не false при аргументе null.");
    }

    @DisplayName("replaceEpic(epic) должен возвращать false, если индекс эпика не существует в менеджере")
    @ParameterizedTest(name = "{index} replaceEpic({0}) должен возвращать false")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void shouldReturnFalseIfReplacingEpicIdDoesNotExist(int id) {
        taskManager.addTaskOfAnyType(epicA);
        epicB = new Epic(id, "Epic B", "Epic B description");

        assertFalse(taskManager.replaceEpic(epicB),
                "Метод возвращает не false при несуществующем id у заменяющего эпика.");
    }

    @Test
    @DisplayName("Тест на стандартную замену подзадачи")
    void replaceSubtaskTest() {
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        int id = taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(id, "Subtask B", TaskStatus.IN_PROGRESS, "Subtask B description", epicA);

        assertTrue(taskManager.replaceSubtask(subtaskB),
                "Метод возвращает не true при стандартной замене подзадачи.");
        assertEquals(subtaskB, taskManager.getSubtask(id), "Подзадачи не совпадают.");
    }

    @Test
    @DisplayName("replaceSubtask(subtask) должен возвращать false, если эпик подзадачи не содержит эту подзадачу")
    void shouldReturnFalseIfReplacingSubtasksEpicDoesNotContainsThisSubtask() {
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        epicB = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicB);
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        int id = taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(id, "Subtask B", TaskStatus.IN_PROGRESS, "Subtask B description", epicB);

        assertFalse(taskManager.replaceSubtask(subtaskB),
                "Метод возвращает не false при неправильном эпике в заменяющей подзадаче.");
    }

    @Test
    @DisplayName("replaceSubtask(subtask) должен возвращать false, если эпик заменяющей подзадачи - null")
    void shouldReturnFalseIfReplacingSubtasksEpicIsNull() {
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        int id = taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(id, "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", null);

        assertFalse(taskManager.replaceSubtask(subtaskB),
                "Метод возвращает не false при null-эпике в заменяющей подзадаче.");
    }

    @Test
    @DisplayName("replaceSubtask(null) должен возвращать false")
    void shouldReturnFalseIfReplacingSubtaskIsNull() {
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        taskManager.addTaskOfAnyType(subtaskA);
        assertFalse(taskManager.replaceSubtask(null), "Метод возвращает не false при аргументе null.");
    }

    @DisplayName("replaceSubtask(subtask) должен возвращать false, если индекс подзадачи не существует в менеджере")
    @ParameterizedTest(name = "{index} replaceSubtask({0}) должен возвращать false")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void shouldReturnFalseIfReplacingSubtaskIdDoesNotExist(int id) {
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask(id, "Subtask B", TaskStatus.IN_PROGRESS,
                "Subtask B description", epicA);

        assertFalse(taskManager.replaceEpic(epicB),
                "Метод возвращает не false при несуществующем id у заменяющей подзадачи.");
    }

    // TODO: Комментарий для ревью (удалить после спринта 6) - Тестирование удаления задач, эпиков и подзадач
    //  реализовано в одном методе, поскольку удалением занимается также один метод, а логика тестирования удаления
    //  подзадач и эпиков связана между собой.

    @Test
    @DisplayName("Тест на стандартное удаление задач, эпиков и подзадач")
    void removeTaskTest() {
        int taskId = taskManager.addTaskOfAnyType(taskA);
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        int epicId = epicA.getId();
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
        int subtaskAId = taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask("Subtask B", TaskStatus.IN_PROGRESS, "Subtask B description", epicA);
        taskManager.addTaskOfAnyType(subtaskB);

        taskManager.removeTaskOfAnyTypeById(taskId);
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач не пуст.");
        taskManager.removeTaskOfAnyTypeById(subtaskAId);
        assertEquals(taskManager.getSubtasks().size(), 1,
                "Список подзадач после удаления подзадачи сократился не на 1 элемент.");
        taskManager.removeTaskOfAnyTypeById(epicId);
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков не пуст.");
        assertTrue(taskManager.getSubtasks().isEmpty(),
                "Список подзадач не пуст после удаления эпика, содержавшего подзадачу.");
    }


    @DisplayName("removeTask(id) не должен ничего удалять, если индекс не существует в менеджере")
    @ParameterizedTest(name = "{index} removeTaskOfAnyTypeById({0}) не должен удалять задачи")
    @MethodSource("nonExistentIdStreamForMapWithFewItems")
    void shouldDoNotRemoveAnythingIfRemovingIdDoesNotExist(int id) {
        taskManager.addTaskOfAnyType(taskA);
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
        subtaskA = new Subtask("Subtask A", TaskStatus.NEW, "Subtask A description", epicA);
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

    private static Stream<Arguments> nonExistentIdStreamForEmptyMap() {
        return Stream.of(-1000000, -1, 0, 1, 1000000).map(Arguments::of);
    }

    private static Stream<Arguments> nonExistentIdStreamForMapWithFewItems() {
        return Stream.of(-1000000, -1, 0, 11, 1000000).map(Arguments::of);
    }
}