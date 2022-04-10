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
        TaskManager taskManager = Managers.getDefault();
        Task task; // Объект для формирования задачи и передачи в методы TaskManager
        Epic epic; // Объект для формирования эпика и передачи в методы TaskManager
        Subtask subtask; // Объект для формирования подзадачи и передачи в методы TaskManager

        /*========= Создание тестовых задач, эпиков и подзадач =========*/

        // Создание двух задач
        task = new Task(taskManager.getNextTaskId(), "Покормить кота", TaskStatus.NEW,
                "Дать коту в обед четверть банки корма «Berkley»");
        int idFeedTheCatTask = taskManager.addTaskOfAnyType(task);
        task = new Task(taskManager.getNextTaskId(), "Попить чаю с булками", TaskStatus.NEW,
                "Съесть ещё этих мягких французских булок да выпить чаю");
        int idEatBunsDrinkTeaTask = taskManager.addTaskOfAnyType(task);

        // Создание эпика с двумя подзадачами
        epic = new Epic(taskManager.getNextTaskId(), "Запроектировать трёхэтажный каркас",
                "Выполнить проект каркаса трёхэтажного административного здания");
        int idDesignStructureEpic = taskManager.addTaskOfAnyType(epic);
        subtask = new Subtask(taskManager.getNextTaskId(), "Рассчитать плоскую раму", TaskStatus.NEW,
                "Выполнить статический расчёт плоской рамы и подобрать сечения элементов",
                epic);
        int idDesignFrameSubtask = taskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask(taskManager.getNextTaskId(), "Выполнить пространственный расчёт",
                TaskStatus.NEW,
                "Выполнить пространственный расчёт с учётом действия пульсационных ветровых нагрузок",
                epic);
        int idDesign3DSubtask = taskManager.addTaskOfAnyType(subtask);

        // Создание эпика с одной подзадачей
        epic = new Epic(taskManager.getNextTaskId(), "Выполнить ТО автомобиля",
                null);
        int idCarMaintenanceEpic = taskManager.addTaskOfAnyType(epic);
        subtask = new Subtask(taskManager.getNextTaskId(), "Поменять масло", TaskStatus.NEW,
                null, epic);
        int idOilChangeSubtask = taskManager.addTaskOfAnyType(subtask);

        /*========= Имитация просмотра тестовых задач, эпиков и подзадач =========*/

        taskManager.getTask(idFeedTheCatTask);
        taskManager.getTask(idEatBunsDrinkTeaTask);
        taskManager.getEpic(idCarMaintenanceEpic);
        taskManager.getSubtask(idOilChangeSubtask);
        taskManager.getTask(idFeedTheCatTask); // повторный просмотр
        taskManager.getTask(idEatBunsDrinkTeaTask); // повторный просмотр
        taskManager.getEpic(idDesignStructureEpic);
        taskManager.getSubtask(idDesignFrameSubtask);
        taskManager.getSubtask(idDesign3DSubtask);
        taskManager.getTask(idFeedTheCatTask); // повторный просмотр

        /*========= Распечатка истории просмотра =========*/

        System.out.println("\n----- История просмотренных задач -----");
        printHistory(taskManager.getHistory());

        /*========= Просмотр 11-й задачи и распечатка истории (для проверки перемещения очереди) =========*/

        taskManager.getTask(idEatBunsDrinkTeaTask); // повторный просмотр
        System.out.println("\n----- Новая история задач после ещё одного просмотра -----");
        printHistory(taskManager.getHistory());
    }
}