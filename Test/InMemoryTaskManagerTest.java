import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.tasktracker.manager.InMemoryTaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    @Override
    void init(){
        taskManager = new InMemoryTaskManager();
        super.init();
    }
}
