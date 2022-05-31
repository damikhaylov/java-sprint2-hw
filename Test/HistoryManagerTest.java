import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.tasktracker.manager.HistoryManager;
import ru.yandex.practicum.tasktracker.manager.InMemoryHistoryManager;
import ru.yandex.practicum.tasktracker.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    protected HistoryManager historyManager;
    protected Task taskA;
    protected Epic epicA;
    protected Subtask subtaskA;
    List<Task> history;


    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
        taskA = new Task(1, "Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.now(), 15);
        epicA = new Epic(2, "Epic A", "Epic A description");
        subtaskA = new Subtask(3, "Subtask A", TaskStatus.NEW, "Subtask A description",
                LocalDateTime.now(), 15, epicA);
        historyManager.add(taskA);
        historyManager.add(epicA);
        historyManager.add(subtaskA);
        history = historyManager.getHistory();
    }

    @Test
    @DisplayName("Тест на стандартное добавление задач в историю")
    void addTest() {
        assertEquals(3, history.size(),
                "Размер истории не соответствует количеству добавленных задач.");
    }

    @Test
    @DisplayName("Тест на добавление в историю пустого элемента")
    void addNullTest() {
        historyManager.add(null);
        assertEquals(3, historyManager.getHistory().size(),
                "Размер истории не соответствует количеству добавленных задач.");
    }

    @Test
    @DisplayName("Тест на добавление в историю дублирующего элемента")
    void addDoubleTest() {
        historyManager.add(epicA);
        history = historyManager.getHistory();
        assertEquals(3, history.size(),
                "Размер истории не соответствует количеству добавленных задач.");
        assertEquals(epicA, history.get(history.size() - 1),
                "Дублирующий элемент не перемещён в конец истории.");
    }

    @Test
    @DisplayName("Тест на получение пустого списка истории")
    void getEmptyHistoryTest() {
        historyManager.remove(taskA.getId());
        historyManager.remove(epicA.getId());
        historyManager.remove(subtaskA.getId());

        history = historyManager.getHistory();
        assertNotNull(history, "Список - null.");
        assertTrue(history.isEmpty(), "Список не пуст.");
    }

    @Test
    @DisplayName("Тест на удаление из истории начального элемента")
    void removeFirstTest() {
        historyManager.remove(taskA.getId());
        history = historyManager.getHistory();
        assertEquals(2, history.size(),
                "Размер истории не соответствует количеству добавленных задач.");
        assertEquals(epicA, history.get(0),
                "В начале истории находится неочередной элемент");
    }

    @Test
    @DisplayName("Тест на удаление из истории конечного элемента")
    void removeLastTest() {
        historyManager.remove(subtaskA.getId());
        history = historyManager.getHistory();
        assertEquals(2, history.size(),
                "Размер истории не соответствует количеству добавленных задач.");
        assertEquals(epicA, history.get(history.size() - 1),
                "В конце истории находится неочередной элемент");
    }

    @Test
    @DisplayName("Тест на удаление из истории серединного элемента")
    void removeMiddleTest() {
        historyManager.remove(epicA.getId());
        history = historyManager.getHistory();
        assertEquals(2, history.size(),
                "Размер истории не соответствует количеству добавленных задач.");
        assertEquals(taskA, history.get(0),
                "В начале истории находится неочередной элемент");
        assertEquals(subtaskA, history.get(history.size() - 1),
                "В конце истории находится неочередной элемент");
    }

    @DisplayName("Тест на удаление из пустой истории")
    @ParameterizedTest(name = "{index} remove({0}) на пустой истории не должен вызывать ошибки")
    @MethodSource("nonExistentIdStreamForEmptyHistory")
    void removeFromEmptyHistory(int id) {
        historyManager = new InMemoryHistoryManager();
        historyManager.remove(id);
        history = historyManager.getHistory();
        assertNotNull(history, "Список - null.");
        assertTrue(history.isEmpty(), "Список не пуст.");
    }

    @DisplayName("Тест на удаление несуществующих элементов из истории")
    @ParameterizedTest(name = "{index} remove({0}) не должен вызывать ошибки")
    @MethodSource("nonExistentIdStreamForHistoryWithFewItems")
    void removeNonExistentFromNonEmptyHistory(int id) {
        historyManager.remove(id);
        assertEquals(3, historyManager.getHistory().size(),
                "Размер истории не соответствует количеству добавленных задач.");
    }

    private static Stream<Arguments> nonExistentIdStreamForEmptyHistory() {
        return Stream.of(-1000000, -1, 0, 1, 1000000).map(Arguments::of);
    }

    private static Stream<Arguments> nonExistentIdStreamForHistoryWithFewItems() {
        return Stream.of(-1000000, -1, 0, 11, 1000000).map(Arguments::of);
    }
}
