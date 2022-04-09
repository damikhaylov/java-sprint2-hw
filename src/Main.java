import ru.yandex.practicum.tasktracker.manager.InMemoryTaskManager;
import ru.yandex.practicum.tasktracker.model.*;

public class Main {
    private static void printTaskLists(InMemoryTaskManager inMemoryTaskManager) {
        System.out.println("\nСписок эпиков:");
        System.out.println(inMemoryTaskManager.getEpics());

        System.out.println("\nСписок задач:");
        System.out.println(inMemoryTaskManager.getTasks());

        System.out.println("\nСписок подзадач:");
        System.out.println(inMemoryTaskManager.getSubtasks());
    }

    public static void main(String[] args) {
        /*========= Создание тестовых задач, эпиков и подзадач =========*/

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
                inMemoryTaskManager.getEpic(idDesignStructureEpic));
        int idDesignFrameSubtask = inMemoryTaskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask(inMemoryTaskManager.getNextTaskId(), "Выполнить пространственный расчёт",
                TaskStatus.NEW,
                "Выполнить пространственный расчёт с учётом действия пульсационных ветровых нагрузок",
                inMemoryTaskManager.getEpic(idDesignStructureEpic));
        int idDesign3DSubtask = inMemoryTaskManager.addTaskOfAnyType(subtask);

        // Создание эпика с одной подзадачей
        epic = new Epic(inMemoryTaskManager.getNextTaskId(), "Выполнить ТО автомобиля",
                null);
        int idCarMaintenanceEpic = inMemoryTaskManager.addTaskOfAnyType(epic);
        subtask = new Subtask(inMemoryTaskManager.getNextTaskId(), "Поменять масло", TaskStatus.NEW,
                null, inMemoryTaskManager.getEpic(idCarMaintenanceEpic));
        int idOilChangeSubtask = inMemoryTaskManager.addTaskOfAnyType(subtask);

        System.out.println("\n----- Списки всех типов задач после создания объектов -----");
        printTaskLists(inMemoryTaskManager);

        /*========= Изменение статусов созданных объектов =========*/

        task = inMemoryTaskManager.getTask(idFeedTheCatTask);
        task = new Task(task.getId(), task.getName(), TaskStatus.DONE, task.getDescription());
        inMemoryTaskManager.replaceTask(task);

        task = inMemoryTaskManager.getTask(idEatBunsDrinkTeaTask);
        task = new Task(task.getId(), task.getName(), TaskStatus.IN_PROGRESS, task.getDescription());
        inMemoryTaskManager.replaceTask(task);

        subtask = inMemoryTaskManager.getSubtask(idDesignFrameSubtask);
        subtask = new Subtask(subtask.getId(), subtask.getName(), TaskStatus.DONE, subtask.getDescription(),
                subtask.getEpic());
        inMemoryTaskManager.replaceSubtask(subtask);

        subtask = inMemoryTaskManager.getSubtask(idDesign3DSubtask);
        subtask = new Subtask(subtask.getId(), subtask.getName(), TaskStatus.IN_PROGRESS, subtask.getDescription(),
                subtask.getEpic());
        inMemoryTaskManager.replaceSubtask(subtask);

        subtask = inMemoryTaskManager.getSubtask(idOilChangeSubtask);
        subtask = new Subtask(subtask.getId(), subtask.getName(), TaskStatus.DONE, subtask.getDescription(),
                subtask.getEpic());
        inMemoryTaskManager.replaceSubtask(subtask);

        System.out.println("\n----- Списки всех типов задач после изменения статусов -----");
        printTaskLists(inMemoryTaskManager);

        /*========= Удаление одной задачи и одного эпика =========*/

        inMemoryTaskManager.deleteTaskOfAnyTypeById(idEatBunsDrinkTeaTask);
        inMemoryTaskManager.deleteTaskOfAnyTypeById(idCarMaintenanceEpic);

        System.out.println("\n----- Списки всех типов задач после удаления одной задачи и эпика -----");
        printTaskLists(inMemoryTaskManager);
    }
}