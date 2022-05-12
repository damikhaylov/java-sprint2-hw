package ru.yandex.practicum.tasktracker.test;

import ru.yandex.practicum.tasktracker.manager.FileBackedTasksManager;
import ru.yandex.practicum.tasktracker.manager.TaskManager;
import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;
import ru.yandex.practicum.tasktracker.model.TaskStatus;

public class TestScenario {
    private final TaskManager taskManager;
    private int idFeedTheCatTask;
    private int idEatBunsDrinkTeaTask;
    private int idDesignStructureEpic;
    private int idDesignFrameSubtask;
    private int idDesign3DSubtask;
    private int idMakeDrawing;
    private int idCarMaintenanceEpic;

    public TestScenario(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * Создание тестовых задач, эпиков и подзадач
     */
    public void Add2Tasks2Epics3Subtasks() {
        Task task; // Объект для формирования задачи и передачи в методы TaskManager
        Epic epic; // Объект для формирования эпика и передачи в методы TaskManager
        Subtask subtask; // Объект для формирования подзадачи и передачи в методы TaskManager

        // Создание двух задач
        task = new Task(taskManager.getNextTaskId(), "Покормить кота", TaskStatus.NEW,
                "Дать коту в обед четверть банки корма «Berkley»");
        idFeedTheCatTask = taskManager.addTaskOfAnyType(task);
        task = new Task(taskManager.getNextTaskId(), "Попить чаю с булками", TaskStatus.DONE,
                "Съесть ещё этих мягких французских булок да выпить чаю");
        idEatBunsDrinkTeaTask = taskManager.addTaskOfAnyType(task);

        // Создание эпика с тремя подзадачами
        epic = new Epic(taskManager.getNextTaskId(), "Запроектировать трёхэтажный каркас",
                "Выполнить проект каркаса трёхэтажного административного здания");
        idDesignStructureEpic = taskManager.addTaskOfAnyType(epic);
        subtask = new Subtask(taskManager.getNextTaskId(), "Рассчитать плоскую раму", TaskStatus.DONE,
                "Выполнить статический расчёт плоской рамы и подобрать сечения элементов",
                epic);
        idDesignFrameSubtask = taskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask(taskManager.getNextTaskId(), "Выполнить пространственный расчёт",
                TaskStatus.DONE,
                "Выполнить пространственный расчёт с учётом действия пульсационных ветровых нагрузок",
                epic);
        idDesign3DSubtask = taskManager.addTaskOfAnyType(subtask);
        subtask = new Subtask(taskManager.getNextTaskId(), "Сделать чертёж",
                TaskStatus.DONE,
                "Сделать чертёж со схемой каркаса",
                epic);
        idMakeDrawing = taskManager.addTaskOfAnyType(subtask);

        // Создание эпика без подзадач
        epic = new Epic(taskManager.getNextTaskId(), "Выполнить ТО автомобиля",
                "Выполнить техобслуживание автомобиля");
        idCarMaintenanceEpic = taskManager.addTaskOfAnyType(epic);
    }

    /**
     * Имитация просмотра тестовых задач и эпика
     */
    public void View2Tasks1Epic() {
        taskManager.getTask(idFeedTheCatTask);
        taskManager.getTask(idEatBunsDrinkTeaTask);
        taskManager.getEpic(idCarMaintenanceEpic);
    }

    /**
     * Имитация повторного просмотра задачи и эпика
     */
    public void View1Task1EpicAgain() {
        taskManager.getTask(idFeedTheCatTask); // повторный просмотр
        taskManager.getEpic(idCarMaintenanceEpic);  // повторный просмотр
    }

    /**
     * Удаление одной из просмотренных задач
     */
    public void Remove1ViewedTask() {
        taskManager.removeTaskOfAnyTypeById(idEatBunsDrinkTeaTask);
    }

    /**
     * Удаление оставшихся просмотренных задач
     */
    public void RemoveAnotherViewedTasks() {
        taskManager.removeTaskOfAnyTypeById(idFeedTheCatTask);
        taskManager.removeTaskOfAnyTypeById(idCarMaintenanceEpic);
    }

    /**
     * Имитация просмотра эпика и его подзадач
     */
    public void ViewEpicWithSubtasks() {
        taskManager.getEpic(idDesignStructureEpic);
        taskManager.getSubtask(idDesignFrameSubtask);
        taskManager.getSubtask(idDesign3DSubtask);
        taskManager.getSubtask(idMakeDrawing);
    }

    /**
     * Имитация повторного просмотра эпика и его подзадачи
     */
    public void ViewEpicAndSubtaskAgain() {
        taskManager.getSubtask(idDesignFrameSubtask); // повторный просмотр
        taskManager.getEpic(idDesignStructureEpic); // повторный просмотр
    }

    /**
     * Удаление просмотренного эпика
     */
    public void Remove1ViewedEpic() {
        taskManager.removeTaskOfAnyTypeById(idDesignStructureEpic);
    }

    public void printHistory() {
        for (Task anyTypeTask : taskManager.getHistory()) {
            System.out.println(FileBackedTasksManager.toString(anyTypeTask));
        }
    }
}
