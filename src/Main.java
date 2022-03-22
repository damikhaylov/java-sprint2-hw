import ru.yandex.practicum.tasktracker.manager.TaskManager;
import ru.yandex.practicum.tasktracker.model.*;

public class Main {
    private static void printTaskLists(TaskManager taskManager) {
        System.out.println("\nСписок эпиков:");
        System.out.println(taskManager.getEpics());

        System.out.println("\nСписок задач:");
        System.out.println(taskManager.getTasks());

        System.out.println("\nСписок подзадач:");
        System.out.println(taskManager.getSubtasks());
    }

    public static void main(String[] args) {
        /*========= Создание тестовых задач, эпиков и подзадач =========*/

        TaskManager taskManager = new TaskManager();
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
                taskManager.getEpicById(idDesignStructureEpic));
        int idDesignFrameSubtask = taskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask(taskManager.getNextTaskId(), "Выполнить пространственный расчёт",
                TaskStatus.NEW,
                "Выполнить пространственный расчёт с учётом действия пульсационных ветровых нагрузок",
                taskManager.getEpicById(idDesignStructureEpic));
        int idDesign3DSubtask = taskManager.addTaskOfAnyType(subtask);

        // Создание эпика с одной подзадачей
        epic = new Epic(taskManager.getNextTaskId(), "Выполнить ТО автомобиля",
                null);
        int idCarMaintenanceEpic = taskManager.addTaskOfAnyType(epic);
        subtask = new Subtask(taskManager.getNextTaskId(), "Поменять масло", TaskStatus.NEW,
                null, taskManager.getEpicById(idCarMaintenanceEpic));
        int idOilChangeSubtask = taskManager.addTaskOfAnyType(subtask);

        System.out.println("\n----- Списки всех типов задач после создания объектов -----");
        printTaskLists(taskManager);

        /*========= Изменение статусов созданных объектов =========*/

        task = taskManager.getTaskById(idFeedTheCatTask);
        task = new Task(task.getId(), task.getName(), TaskStatus.DONE, task.getDescription());
        taskManager.replaceTask(task);

        task = taskManager.getTaskById(idEatBunsDrinkTeaTask);
        task = new Task(task.getId(), task.getName(), TaskStatus.IN_PROGRESS, task.getDescription());
        taskManager.replaceTask(task);

        subtask = taskManager.getSubtaskById(idDesignFrameSubtask);
        subtask = new Subtask(subtask.getId(), subtask.getName(), TaskStatus.DONE, subtask.getDescription(),
                subtask.getEpic());
        taskManager.replaceSubtask(subtask);

        subtask = taskManager.getSubtaskById(idDesign3DSubtask);
        subtask = new Subtask(subtask.getId(), subtask.getName(), TaskStatus.IN_PROGRESS, subtask.getDescription(),
                subtask.getEpic());
        taskManager.replaceSubtask(subtask);

        subtask = taskManager.getSubtaskById(idOilChangeSubtask);
        subtask = new Subtask(subtask.getId(), subtask.getName(), TaskStatus.DONE, subtask.getDescription(),
                subtask.getEpic());
        taskManager.replaceSubtask(subtask);

        System.out.println("\n----- Списки всех типов задач после изменения статусов -----");
        printTaskLists(taskManager);

        /*========= Удаление одной задачи и одного эпика =========*/

        taskManager.deleteTaskOfAnyTypeById(idEatBunsDrinkTeaTask);
        taskManager.deleteTaskOfAnyTypeById(idCarMaintenanceEpic);

        System.out.println("\n----- Списки всех типов задач после удаления одной задачи и эпика -----");
        printTaskLists(taskManager);
    }
}