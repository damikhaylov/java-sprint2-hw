import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.tasktracker.manager.InMemoryTaskManager;
import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.tasktracker.model.TaskStatus.*;

public class EpicTest {
    protected InMemoryTaskManager taskManager;
    protected Epic epicA;
    protected Subtask subtaskFirst;
    protected Subtask subtaskMiddle;
    protected Subtask subtaskLast;
    protected Subtask subtaskD;
    // Константы временных характеристик для тестового набора из 3-х подзадач
    protected final LocalDateTime START_TIME_FIRST =
            LocalDateTime.of(2022, 6, 1, 12, 45);
    protected final int DURATION_FIRST = 15;
    protected final LocalDateTime START_TIME_MIDDLE = null;
    protected final int DURATION_MIDDLE = 45;
    protected final LocalDateTime START_TIME_LAST =
            LocalDateTime.of(2022, 6, 2, 15, 15);
    protected final int DURATION_LAST = 20;
    protected final LocalDateTime END_TIME_LAST = START_TIME_LAST.plusMinutes(DURATION_LAST);
    protected final int DURATION_SUM = DURATION_FIRST + DURATION_MIDDLE + DURATION_LAST;

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
        epicA = new Epic(1, "Epic A", "Epic A description");
        taskManager.addTaskOfAnyType(epicA);
    }

    @DisplayName("Тест на изменение статуса эпика при добавлении подзадач")
    @ParameterizedTest(name = "{index} Статус эпика: {3}")
    @MethodSource("taskStatusStreamForAddingSubtasks")
    void changingEpicStatusAfterAddingSubtasksTest(TaskStatus subStatusA, TaskStatus subStatusB, TaskStatus subStatusC,
                                                   TaskStatus epicStatus) {

        Add3Subtasks(subStatusA, subStatusB, subStatusC);
        assertEquals(epicStatus, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @DisplayName("Тест на изменение статуса эпика при удалении подзадачи")
    @ParameterizedTest(name = "{index} Статус эпика: {3}")
    @MethodSource("taskStatusStreamForRemovingSubtask")
    void changingEpicStatusAfterRemovingSubtaskTest(TaskStatus subStatusA, TaskStatus subStatusB, TaskStatus subStatusC,
                                                    TaskStatus epicStatus) {

        Add3Subtasks(subStatusA, subStatusB, subStatusC);
        taskManager.removeTaskOfAnyTypeById(subtaskLast.getId());
        assertEquals(epicStatus, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @DisplayName("Тест на изменение статуса эпика при замене подзадачи")
    @ParameterizedTest(name = "{index} Статус эпика: {4}")
    @MethodSource("taskStatusStreamForReplacingSubtask")
    void changingEpicStatusAfterReplacingSubtaskTest(TaskStatus subStatusA, TaskStatus subStatusB,
                                                     TaskStatus subStatusC, TaskStatus replacingStatus,
                                                     TaskStatus epicStatus) {

        Add3Subtasks(subStatusA, subStatusB, subStatusC);
        subtaskD = new Subtask(subtaskLast.getId(), "Subtask D", replacingStatus, "Subtask D description",
                null, 0, epicA);
        taskManager.replaceSubtask(subtaskD);
        assertEquals(epicStatus, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @Test
    @DisplayName("Тест на изменение статуса эпика на NEW при поочерёдном удалении его подзадач")
    void changingEpicStatusToNewAfterRemovingSubtasksTest() {
        Add3Subtasks(NEW, IN_PROGRESS, DONE);

        taskManager.removeTaskOfAnyTypeById(subtaskFirst.getId());
        taskManager.removeTaskOfAnyTypeById(subtaskMiddle.getId());
        taskManager.removeTaskOfAnyTypeById(subtaskLast.getId());
        assertEquals(NEW, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @Test
    @DisplayName("Тест на изменение статуса эпика на NEW при удалении всех подзадач в менеджере")
    void changingEpicStatusToNewAfterRemovingAllSubtasksTest() {
        Add3Subtasks();
        taskManager.removeAllSubtasks();
        assertEquals(NEW, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @Test
    @DisplayName("Тест на начальные значения времён эпика")
    void setEpicDefaultTimesAfterCreationTest() {
        assertEquals(0, epicA.getDuration(),
                "Длительность эпика не 0.");
        assertNull(epicA.getStartTime(), "Начало эпика не null.");
        assertNull(epicA.getEndTime(), "Конец эпика не null.");
    }

    @Test
    @DisplayName("Тест на изменение времён эпика при добавлении подзадач")
    void changingEpicTimesAfterAddingSubtasksTest() {
        Add3Subtasks();
        assertEquals(DURATION_SUM, epicA.getDuration(),
                "Длительность эпика не соответствует длительности подзадач.");
        assertEquals(START_TIME_FIRST, epicA.getStartTime(),
                "Начало эпика не соответствует началу самой ранней подзадачи.");
        assertEquals(END_TIME_LAST, epicA.getEndTime(),
                "Конец эпика не соответствует концу самой поздней подзадачи.");
    }

    @Test
    @DisplayName("Тест на изменение времён эпика при удалении самой ранней подзадачи")
    void changingEpicTimesAfterRemovingFirstSubtaskTest() {
        Add3Subtasks(); // Серединная задача имеет startTime = null;
        taskManager.removeTaskOfAnyTypeById(subtaskFirst.getId());
        assertEquals(DURATION_MIDDLE + DURATION_LAST, epicA.getDuration(),
                "Длительность эпика не соответствует длительности подзадач.");
        assertEquals(subtaskLast.getStartTime(), epicA.getStartTime(),
                "Начало эпика не соответствует началу самой ранней подзадачи.");
        assertEquals(subtaskLast.getEndTime(), epicA.getEndTime(),
                "Конец эпика не соответствует концу самой поздней подзадачи.");
    }

    @Test
    @DisplayName("Тест на изменение времён эпика при удалении самой поздней подзадачи")
    void changingEpicTimesAfterRemovingLastSubtaskTest() {
        Add3Subtasks();  // Серединная задача имеет startTime = null;
        taskManager.removeTaskOfAnyTypeById(subtaskLast.getId());
        assertEquals(DURATION_FIRST + DURATION_MIDDLE, epicA.getDuration(),
                "Длительность эпика не соответствует длительности подзадач.");
        assertEquals(subtaskFirst.getStartTime(), epicA.getStartTime(),
                "Начало эпика не соответствует началу самой ранней подзадачи.");
        assertEquals(subtaskFirst.getEndTime(), epicA.getEndTime(),
                "Конец эпика не соответствует концу самой поздней подзадачи.");
    }

    @Test
    @DisplayName("Тест на изменение времён эпика при замене самой ранней подзадачи")
    void changingEpicTimesAfterReplacingFirstSubtaskTest() {
        Add3Subtasks();  // Серединная задача имеет startTime = null;
        subtaskD = new Subtask(subtaskFirst.getId(), "Subtask D", NEW, "Subtask D description",
                START_TIME_FIRST.minusDays(1), DURATION_FIRST + 15, epicA);
        taskManager.replaceSubtask(subtaskD);
        assertEquals(DURATION_SUM + 15, epicA.getDuration(),
                "Длительность эпика не соответствует длительности подзадач.");
        assertEquals(subtaskD.getStartTime(), epicA.getStartTime(),
                "Начало эпика не соответствует началу самой ранней подзадачи.");
        assertEquals(END_TIME_LAST, epicA.getEndTime(),
                "Конец эпика не соответствует концу самой поздней подзадачи.");
    }

    @Test
    @DisplayName("Тест на изменение времён эпика при замене самой поздней подзадачи")
    void changingEpicTimesAfterReplacingLastSubtaskTest() {
        Add3Subtasks();  // Серединная задача имеет startTime = null;
        subtaskD = new Subtask(subtaskLast.getId(), "Subtask D", NEW, "Subtask D description",
                START_TIME_LAST.plusDays(1), DURATION_LAST + 15, epicA);
        taskManager.replaceSubtask(subtaskD);
        assertEquals(DURATION_SUM + 15, epicA.getDuration(),
                "Длительность эпика не соответствует длительности подзадач.");
        assertEquals(START_TIME_FIRST, epicA.getStartTime(),
                "Начало эпика не соответствует началу самой ранней подзадачи.");
        assertEquals(subtaskD.getEndTime(), epicA.getEndTime(),
                "Конец эпика не соответствует концу самой поздней подзадачи.");
    }

    @Test
    @DisplayName("Тест на изменение времён эпика при удалении всех подзадач")
    void changingEpicTimesAfterRemovingAllSubtasksTest() {
        Add3Subtasks();
        taskManager.removeAllSubtasks();
        assertEquals(0, epicA.getDuration(), "Длительность эпика не 0.");
        assertNull(epicA.getStartTime(), "Начало эпика не null.");
        assertNull(epicA.getEndTime(), "Конец эпика не null.");
    }

    private void Add3Subtasks(TaskStatus statusA, TaskStatus statusB, TaskStatus statusC) {
        subtaskFirst = new Subtask(2, "Subtask A", statusA, "Subtask A description",
                START_TIME_FIRST, DURATION_FIRST, epicA);
        taskManager.addTaskOfAnyType(subtaskFirst);
        subtaskMiddle = new Subtask(3, "Subtask B", statusB, "Subtask B description",
                START_TIME_MIDDLE, DURATION_MIDDLE, epicA);
        taskManager.addTaskOfAnyType(subtaskMiddle);
        subtaskLast = new Subtask(4, "Subtask C", statusC, "Subtask C description",
                START_TIME_LAST, DURATION_LAST, epicA);
        taskManager.addTaskOfAnyType(subtaskLast);
    }

    private void Add3Subtasks() {
        Add3Subtasks(NEW, IN_PROGRESS, DONE);
    }

    private static Stream<Arguments> taskStatusStreamForAddingSubtasks() {
        return Stream.of(
                Arguments.of(NEW, NEW, NEW, NEW),
                Arguments.of(DONE, DONE, DONE, DONE),
                Arguments.of(IN_PROGRESS, IN_PROGRESS,
                        IN_PROGRESS, IN_PROGRESS),
                Arguments.of(NEW, IN_PROGRESS, DONE, IN_PROGRESS),
                Arguments.of(NEW, NEW, DONE, IN_PROGRESS),
                Arguments.of(NEW, DONE, DONE, IN_PROGRESS),
                Arguments.of(NEW, NEW, IN_PROGRESS, IN_PROGRESS),
                Arguments.of(NEW, IN_PROGRESS, IN_PROGRESS, IN_PROGRESS),
                Arguments.of(DONE, DONE, IN_PROGRESS, IN_PROGRESS),
                Arguments.of(DONE, IN_PROGRESS, IN_PROGRESS, IN_PROGRESS)
        );
    }

    private static Stream<Arguments> taskStatusStreamForRemovingSubtask() {
        return Stream.of(
                Arguments.of(NEW, NEW, NEW, NEW),
                Arguments.of(DONE, DONE, DONE, DONE),
                Arguments.of(IN_PROGRESS, IN_PROGRESS,
                        IN_PROGRESS, IN_PROGRESS),
                Arguments.of(NEW, IN_PROGRESS, DONE, IN_PROGRESS),
                Arguments.of(NEW, NEW, DONE, NEW),
                Arguments.of(DONE, DONE, NEW, DONE),
                Arguments.of(NEW, NEW, IN_PROGRESS, NEW),
                Arguments.of(IN_PROGRESS, IN_PROGRESS, NEW, IN_PROGRESS),
                Arguments.of(DONE, DONE, IN_PROGRESS, DONE),
                Arguments.of(IN_PROGRESS, IN_PROGRESS, DONE, IN_PROGRESS)
        );
    }

    private static Stream<Arguments> taskStatusStreamForReplacingSubtask() {
        return Stream.of(
                Arguments.of(NEW, NEW, NEW, DONE, IN_PROGRESS),
                Arguments.of(DONE, DONE, DONE, NEW, IN_PROGRESS),
                Arguments.of(IN_PROGRESS, IN_PROGRESS,
                        IN_PROGRESS, DONE, IN_PROGRESS),
                Arguments.of(NEW, DONE, IN_PROGRESS, DONE, IN_PROGRESS),
                Arguments.of(NEW, NEW, DONE, NEW, NEW),
                Arguments.of(DONE, DONE, NEW, DONE, DONE),
                Arguments.of(NEW, NEW, IN_PROGRESS, NEW, NEW),
                Arguments.of(IN_PROGRESS, IN_PROGRESS, NEW, IN_PROGRESS, IN_PROGRESS),
                Arguments.of(DONE, DONE, IN_PROGRESS, DONE, DONE),
                Arguments.of(IN_PROGRESS, IN_PROGRESS, DONE, IN_PROGRESS, IN_PROGRESS)
        );
    }
}
