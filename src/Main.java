import ru.yandex.practicum.tasktracker.manager.*;
import ru.yandex.practicum.tasktracker.model.*;

import java.util.List;

public class Main {

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

        // Создание эпика с тремя подзадачами
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
        subtask = new Subtask(taskManager.getNextTaskId(), "Сделать чертёж",
                TaskStatus.NEW,
                "Сделать чертёж со схемой каркаса",
                epic);
        int idMakeDrawing = taskManager.addTaskOfAnyType(subtask);

        // Создание эпика без подзадач
        epic = new Epic(taskManager.getNextTaskId(), "Выполнить ТО автомобиля",
                null);
        int idCarMaintenanceEpic = taskManager.addTaskOfAnyType(epic);

        /*========= Имитация просмотра тестовых задач и эпика =========*/
        taskManager.getTask(idFeedTheCatTask);
        taskManager.getTask(idEatBunsDrinkTeaTask);
        taskManager.getEpic(idCarMaintenanceEpic);
        System.out.println("\n----- История просмотренных задач -----");
        printHistory(taskManager.getHistory());

        /*========= Имитация повторного просмотра задачи и эпика =========*/
        taskManager.getTask(idFeedTheCatTask); // повторный просмотр
        taskManager.getEpic(idCarMaintenanceEpic);  // повторный просмотр
        System.out.println("\n----- История просмотренных задач после повторного просмотра -----");
        printHistory(taskManager.getHistory());

        /*========= Удаление одной из просмотренных задач =========*/
        taskManager.deleteTaskOfAnyTypeById(idEatBunsDrinkTeaTask);
        System.out.println("\n----- История просмотренных задач после удаления одной из задач -----");
        printHistory(taskManager.getHistory());

        /*========= Удаление оставшихся просмотренных задач =========*/
        taskManager.deleteTaskOfAnyTypeById(idFeedTheCatTask);
        taskManager.deleteTaskOfAnyTypeById(idCarMaintenanceEpic);
        System.out.println("\n----- История после удаления всех просмотренных задач -----");
        printHistory(taskManager.getHistory());

        /*========= Имитация просмотра эпика и его подзадач =========*/
        taskManager.getEpic(idDesignStructureEpic);
        taskManager.getSubtask(idDesignFrameSubtask);
        taskManager.getSubtask(idDesign3DSubtask);
        taskManager.getSubtask(idMakeDrawing);
        System.out.println("\n----- История после просмотра эпика и его подзадач -----");
        printHistory(taskManager.getHistory());

        /*========= Имитация повторного просмотра эпика и его подзадач =========*/
        taskManager.getSubtask(idDesignFrameSubtask); // повторный просмотр
        taskManager.getEpic(idDesignStructureEpic); // повторный просмотр
        System.out.println("\n----- История после просмотра эпика и его подзадач -----");
        printHistory(taskManager.getHistory());

        /*========= Удаление эпика =========*/
        taskManager.deleteTaskOfAnyTypeById(idDesignStructureEpic);
        System.out.println("\n----- История после удаления эпика (включающего подзадачи) -----");
        printHistory(taskManager.getHistory());
    }

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
}