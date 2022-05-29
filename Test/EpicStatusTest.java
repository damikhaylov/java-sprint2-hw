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

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.tasktracker.model.TaskStatus.*;

public class EpicStatusTest {
    protected InMemoryTaskManager taskManager;
    protected Epic epicA;
    protected Subtask subtaskA;
    protected Subtask subtaskB;
    protected Subtask subtaskC;
    protected Subtask subtaskD;

    // TODO: Комментарий для ревью (удалить после спринта 6) - Поскольку вся логика по вычислению статуса эпиков
    //  по замечаниям код-ревью была перенесена из класса Epic в InMemoryTaskManager, то тесты изменения статусов
    //  выполняются через методы InMemoryTaskManager.

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
        epicA = new Epic("Epic A", "Epic A description");
        epicA = (Epic) taskManager.addTaskOfAnyTypeAndReturnTask(epicA);
    }

    @DisplayName("Тест на изменение статуса эпика при добавлении подзадач")
    @ParameterizedTest(name = "{index} Статус эпика: {3}")
    @MethodSource("taskStatusStreamForAddingSubtasks")
    void shouldChangeEpicStatusAfterAddingSubtask(
            TaskStatus subStatusA, TaskStatus subStatusB, TaskStatus subStatusC, TaskStatus epicStatus) {
        subtaskA = new Subtask("Subtask A", subStatusA, "Subtask A description", epicA);
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask("Subtask B", subStatusB, "Subtask B description", epicA);
        taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = new Subtask("Subtask C", subStatusC, "Subtask C description", epicA);
        taskManager.addTaskOfAnyType(subtaskC);

        assertEquals(epicStatus, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @DisplayName("Тест на изменение статуса эпика при удалении подзадачи")
    @ParameterizedTest(name = "{index} Статус эпика: {3}")
    @MethodSource("taskStatusStreamForRemovingSubtask")
    void shouldChangeEpicStatusAfterRemoveSubtask(
            TaskStatus subStatusA, TaskStatus subStatusB, TaskStatus removingStatus, TaskStatus epicStatus) {
        subtaskA = new Subtask("Subtask A", subStatusA, "Subtask A description", epicA);
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask("Subtask B", subStatusB, "Subtask B description", epicA);
        taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = new Subtask("Subtask C", removingStatus, "Subtask C description", epicA);
        int id = taskManager.addTaskOfAnyType(subtaskC);
        taskManager.removeTaskOfAnyTypeById(id);

        assertEquals(epicStatus, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @DisplayName("Тест на изменение статуса эпика при замене подзадачи")
    @ParameterizedTest(name = "{index} Статус эпика: {4}")
    @MethodSource("taskStatusStreamForReplacingSubtask")
    void shouldChangeEpicStatusAfterReplacingSubtask(TaskStatus subStatusA, TaskStatus subStatusB,
                                                     TaskStatus subStatusC, TaskStatus replacingStatus, TaskStatus epicStatus) {

        subtaskA = new Subtask("Subtask A", subStatusA, "Subtask A description", epicA);
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask("Subtask B", subStatusB, "Subtask B description", epicA);
        taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = new Subtask("Subtask C", subStatusC, "Subtask C description", epicA);
        int id = taskManager.addTaskOfAnyType(subtaskC);
        subtaskD = new Subtask(id, "Subtask D", replacingStatus, "Subtask D description", epicA);

        taskManager.replaceSubtask(subtaskD);

        assertEquals(epicStatus, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @Test
    @DisplayName("Тест на изменение статуса эпика на NEW при удалении его подзадач")
    void shouldChangeEpicStatusToNewAfterRemovingSubtasks() {
        subtaskA = new Subtask("Subtask A", DONE, "Subtask A description", epicA);
        int idA = taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask("Subtask B", DONE, "Subtask B description", epicA);
        int idB =taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = new Subtask("Subtask C", DONE, "Subtask C description", epicA);
        int idC =taskManager.addTaskOfAnyType(subtaskC);

        taskManager.removeTaskOfAnyTypeById(idA);
        taskManager.removeTaskOfAnyTypeById(idB);
        taskManager.removeTaskOfAnyTypeById(idC);

        assertEquals(NEW, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
    }

    @Test
    @DisplayName("Тест на изменение статуса эпика на NEW при удалении всех подзадач в менеджере")
    void shouldChangeEpicStatusToNewAfterRemovingAllSubtasks() {
        subtaskA = new Subtask("Subtask A", DONE, "Subtask A description", epicA);
        taskManager.addTaskOfAnyType(subtaskA);
        subtaskB = new Subtask("Subtask B", DONE, "Subtask B description", epicA);
        taskManager.addTaskOfAnyType(subtaskB);
        subtaskC = new Subtask("Subtask C", DONE, "Subtask C description", epicA);
        taskManager.addTaskOfAnyType(subtaskC);

        taskManager.removeAllSubtasks();

        assertEquals(NEW, epicA.getStatus(),
                "Статус эпика не соответствует обобщённому статусу подзадач.");
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
