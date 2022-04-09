import ru.yandex.practicum.tasktracker.manager.*;
import ru.yandex.practicum.tasktracker.model.*;
import java.util.List;

public class Main {
    private static void printHistory(List<Task> history) {
        for (Task anyTypeTask : history) {
            System.out.print(anyTypeTask.getId() + " " + anyTypeTask.getName());
            if (anyTypeTask.getClass() == Epic.class) {
                System.out.println(" (эпик)");
            } else if (anyTypeTask.getClass() == Subtask.class) {
                System.out.println(" (подзадача)");
            } else {
                System.out.println(" (задача)");
            }
        }
    }

    public static void main(String[] args) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task task; // Объект для формирования задачи и передачи в методы TaskManager
        Epic epic; // Объект для формирования эпика и передачи в методы TaskManager
        Subtask subtask; // Объект для формирования подзадачи и передачи в методы TaskManager

        /*========= Создание тестовых задач, эпиков и подзадач =========*/

        // Создание двух задач
        task = new Task(inMemoryTaskManager.getNextTaskId(), "Покормить кота", TaskStatus.NEW,
                "Дать коту в обед четверть банки корма «Berkley»");
        int idFeedTheCatTask = inMemoryTaskManager.addTaskOfAnyType(task);
        task = new Task(inMemoryTaskManager.getNextTaskId(), "Попить чаю с булками", TaskStatus.NEW,
                "Съесть ещё этих мягких французских булок да выпить чаю");
        int idEatBunsDrinkTeaTask = inMemoryTaskManager.addTaskOfAnyType(task);

        // Создание эпика с двумя подзадачами
        epic = new Epic(inMemoryTaskManager.getNextTaskId(), "Запроектировать трёхэтажный каркас",
                "Выполнить проект каркаса трёхэтажного административного здания");
        int idDesignStructureEpic = inMemoryTaskManager.addTaskOfAnyType(epic);
        subtask = new Subtask(inMemoryTaskManager.getNextTaskId(), "Рассчитать плоскую раму", TaskStatus.NEW,
                "Выполнить статический расчёт плоской рамы и подобрать сечения элементов",
                epic);
        int idDesignFrameSubtask = inMemoryTaskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask(inMemoryTaskManager.getNextTaskId(), "Выполнить пространственный расчёт",
                TaskStatus.NEW,
                "Выполнить пространственный расчёт с учётом действия пульсационных ветровых нагрузок",
                epic);
        int idDesign3DSubtask = inMemoryTaskManager.addTaskOfAnyType(subtask);

        // Создание эпика с одной подзадачей
        epic = new Epic(inMemoryTaskManager.getNextTaskId(), "Выполнить ТО автомобиля",
                null);
        int idCarMaintenanceEpic = inMemoryTaskManager.addTaskOfAnyType(epic);
        subtask = new Subtask(inMemoryTaskManager.getNextTaskId(), "Поменять масло", TaskStatus.NEW,
                null, epic);
        int idOilChangeSubtask = inMemoryTaskManager.addTaskOfAnyType(subtask);

        /*========= Имитация просмотра тестовых задач, эпиков и подзадач =========*/

        inMemoryTaskManager.getTask(idFeedTheCatTask);
        inMemoryTaskManager.getTask(idEatBunsDrinkTeaTask);
        inMemoryTaskManager.getEpic(idCarMaintenanceEpic);
        inMemoryTaskManager.getSubtask(idOilChangeSubtask);
        inMemoryTaskManager.getTask(idFeedTheCatTask); // повторный просмотр
        inMemoryTaskManager.getTask(idEatBunsDrinkTeaTask); // повторный просмотр
        inMemoryTaskManager.getEpic(idDesignStructureEpic);
        inMemoryTaskManager.getSubtask(idDesignFrameSubtask);
        inMemoryTaskManager.getSubtask(idDesign3DSubtask);
        inMemoryTaskManager.getTask(idFeedTheCatTask); // повторный просмотр

        /*========= Распечатка истории просмотра =========*/

        System.out.println("\n----- История просмотренных задач -----");
        printHistory(inMemoryTaskManager.getHistory());

        /*========= Просмотр 11-й задачи и распечатка истории (для проверки перемещения очереди) =========*/

        inMemoryTaskManager.getTask(idEatBunsDrinkTeaTask); // повторный просмотр
        System.out.println("\n----- Новая история задач после кщё одного просмотра -----");
        printHistory(inMemoryTaskManager.getHistory());
    }
}