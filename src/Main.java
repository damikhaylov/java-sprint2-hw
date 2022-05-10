import ru.yandex.practicum.tasktracker.manager.*;
import ru.yandex.practicum.tasktracker.test.TestScenario;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        TestScenario test = new TestScenario(taskManager);

        /*========= Создание тестовых задач, эпиков и подзадач =========*/
        test.Add2Tasks2Epics3Subtasks();

        /*========= Имитация просмотра тестовых задач и эпика =========*/
        test.View2Tasks1Epic();
        System.out.println("\n----- История просмотренных задач -----");
        test.printHistory();

        /*========= Имитация повторного просмотра задачи и эпика =========*/
        test.View1Task1EpicAgain();
        System.out.println("\n----- История просмотренных задач после повторного просмотра -----");
        test.printHistory();

        /*========= Удаление одной из просмотренных задач =========*/
        test.Remove1ViewedTask();
        System.out.println("\n----- История просмотренных задач после удаления одной из задач -----");
        test.printHistory();

        /*========= Удаление оставшихся просмотренных задач =========*/
        test.RemoveAnotherViewedTasks();
        System.out.println("\n----- История после удаления всех просмотренных задач -----");
        test.printHistory();

        /*========= Имитация просмотра эпика и его подзадач =========*/
        test.ViewEpicWithSubtasks();
        System.out.println("\n----- История после просмотра эпика и его подзадач -----");
        test.printHistory();

        /*========= Имитация повторного просмотра эпика и его подзадач =========*/
        test.ViewEpicAndSubtaskAgain();
        System.out.println("\n----- История после повторного просмотра эпика и его подзадач -----");
        test.printHistory();

        /*========= Удаление эпика =========*/
        test.Remove1ViewedEpic();
        System.out.println("\n----- История после удаления эпика (включающего подзадачи) -----");
        test.printHistory();
    }
}