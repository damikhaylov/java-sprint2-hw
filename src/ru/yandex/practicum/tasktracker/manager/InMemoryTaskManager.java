package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class InMemoryTaskManager implements TaskManager {
    protected int nextTaskId; // очередной (ещё не присвоенный) id задачи
    final protected Map<Integer, Task> tasks;
    final protected Map<Integer, Epic> epics;
    final protected Map<Integer, Subtask> subtasks;
    final protected HistoryManager historyManager;

    public InMemoryTaskManager() {
        nextTaskId = 1; // нумерация задач будет начинаться с 1
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    /**
     * Возвращает очередной номер, который может быть присвоен новой задаче в качестве id
     */
    @Override
    public int getNextTaskId() {
        return nextTaskId;
    }

    /**
     * Возвращает все задачи в виде ArrayList c объектами Task
     */
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Возвращает все эпики в виде ArrayList c объектами Epic
     */
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    /**
     * Возвращает все подзадачи в виде ArrayList c объектами Subtask
     */
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    /**
     * Удаляет все задачи
     */
    @Override
    public void removeAllTasks() {
        removeTasksFromHistoryByIDSet(tasks.keySet()); // удаление из истории просмотров
        tasks.clear();
    }

    /**
     * Удаляет все эпики (а также все подзадачи вместе с ними)
     */
    @Override
    public void removeAllEpics() {
        removeTasksFromHistoryByIDSet(subtasks.keySet()); // удаление всех подзадач из истории просмотров
        removeTasksFromHistoryByIDSet(epics.keySet()); // удаление всех эпиков из истории просмотров
        subtasks.clear();
        epics.clear();
    }

    /**
     * Удаляет все подзадачи
     */
    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasksIdSet().clear();
            epic.setStatus(TaskStatus.NEW);
        }
        removeTasksFromHistoryByIDSet(subtasks.keySet()); // удаление всех подзадач из истории просмотров
        subtasks.clear();
    }

    private void removeTasksFromHistoryByIDSet(Set<Integer> idSet) {
        for (Integer id : idSet) {
            historyManager.remove(id);
        }
    }

    /**
     * Возвращает задачу по id, или null, если задачи с таким id не существует
     */
    @Override
    public Task getTask(int id) {
        Task task = tasks.getOrDefault(id, null);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    /**
     * Возвращает эпик по id, или null, если эпика с таким id не существует
     */
    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.getOrDefault(id, null);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    /**
     * Возвращает подзадачу по id, или null, если задачи с таким id не найдено
     */
    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.getOrDefault(id, null);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    /**
     * Добавляет новую задачу, эпик или подзадачу в соответствующие их типу коллекции, если переданный объект не null
     *
     * @param task объект класса Task или классов-наследников Epic и Subtask
     * @return id подзадачи, если она добавлена и 0, если не добавлена
     */
    @Override
    public int addTaskOfAnyType(Task task) {
        task = addTaskOfAnyTypeReturningTask(task);
        return (task != null) ? task.getId() : 0;
    }

    /**
     * Добавляет новую задачу, эпик или подзадачу в соответствующие их типу коллекции, если переданный объект не null
     *
     * @param task объект класса Task или классов-наследников Epic и Subtask
     * @return task объект класса Task или классов-наследников Epic и Subtask или null, если задача не добавлена
     */
    // TODO: Комментарий для ревью (спринт 6) - Метод добавлен, чтобы получать актуальный эпик после добавления эпика
    //  в менеджер, в ходе которого исходный эпик может пересобираться с добавлением очередного id. Возвращаемый
    //  объект-эпик нужен, чтобы использовать его в качестве аргумента в конструкторе дочерних подзадач.
    @Override
    public Task addTaskOfAnyTypeReturningTask(Task task) {
        if (task == null) {
            return null;
        }

        // Если задача передана с дефолтным id, он будет заменён на очередной
        task = replaceDefaultTaskIdWithNextId(task);

        if (task.getId() >= nextTaskId) {
            nextTaskId = task.getId() + 1;
        }

        if (task.getClass() == Epic.class) {
            epics.put(task.getId(), (Epic) task);
        } else if (task.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) task;
            Epic epic = subtask.getEpic();
            subtasks.put(subtask.getId(), subtask);
            epic.getSubtasksIdSet().add(subtask.getId());
            setEpicStatusBySubtasks(epic);
        } else {
            tasks.put(task.getId(), task);
        }
        return task;
    }

    /**
     * Заменяет задачу, если новая задача не null, и если она передаётся с id существующей задачи
     *
     * @param task объект Task
     * @return true, если подзадача добавлена, false, если нет
     */
    @Override
    public boolean replaceTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            tasks.replace(task.getId(), task);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Заменяет эпик, если новый эпик не null, и если он передаётся с id существующего эпика (подзадачи эпика
     * при этом сохраняются)
     *
     * @param epic объект Epic
     * @return true, если эпик добавлен, false, если нет
     */
    @Override
    public boolean replaceEpic(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId())) {
            epics.replace(epic.getId(), epic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Заменяет подзадачу, если новая подзадача не null, если она передаётся с id подзадачи, существующей в эпике
     *
     * @param subtask объект Subtask
     * @return true, если подзадача добавлена, false, если нет
     */
    @Override
    public boolean replaceSubtask(Subtask subtask) {
        if (subtask == null) {
            return false;
        }
        Epic epic = subtask.getEpic();
        if (epic.getSubtasksIdSet().contains(subtask.getId())) {
            subtasks.replace(subtask.getId(), subtask); // замена в таблице подзадач
            setEpicStatusBySubtasks(epic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Удаляет задачу, эпик, подзадачу с заданным id из коллекции, соответствующей их типу
     *
     * @param id id объекта (задачи, эпика, подзадачи)
     */
    @Override
    public void removeTaskOfAnyTypeById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        } else if (epics.containsKey(id)) {
            // удаление всех подзадач эпика из истории просмотра
            removeTasksFromHistoryByIDSet(epics.get(id).getSubtasksIdSet());
            // удаление всех подзадач эпика из таблицы подзадач
            for (Integer subtaskId : epics.get(id).getSubtasksIdSet()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id); // удаление эпика
        } else {
            if (subtasks.containsKey(id)) {
                Epic epic = subtasks.get(id).getEpic();
                epic.getSubtasksIdSet().remove(id); // удаление подзадачи из сета подзадач эпика
                subtasks.remove(id); // удаление подзадачи из таблицы подзадач
                setEpicStatusBySubtasks(epic);
            }
        }
        historyManager.remove(id); // удаление задачи некоторого типа с заданным id также из истории просмотров
    }

    /**
     * Возвращает историю просмотров задач, эпиков подзадач
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    /**
     * Заменяет для задачи любого типа id «по-умолчанию», присваиваемый конструктором, на очередной id,
     * выдаваемый менеджером (задача пересобирается с новым id)
     */
    // TODO: Комментарий для ревью (спринт 6) - Метод добавлен для обработки задач, передаваемых в менеджер без id
    //  (как в примере тестов техзадания)
    private Task replaceDefaultTaskIdWithNextId(Task task) {
        if (task.getId() != Task.DEFAULT_ID) {
            return task;
        }

        if (task.getClass() == Epic.class) {
            task = new Epic(getNextTaskId(), task.getName(), task.getStatus(), task.getDescription());
        } else if (task.getClass() == Subtask.class) {
            task = new Subtask(getNextTaskId(), task.getName(), task.getStatus(), task.getDescription(),
                    ((Subtask) task).getEpic());
        } else {
            task = new Task(getNextTaskId(), task.getName(), task.getStatus(), task.getDescription());
        }

        return task;
    }

    /**
     * Назначает статус эпика на основе статусов его подзадач
     */
    private void setEpicStatusBySubtasks(Epic epic) {
        if (epic.getSubtasksIdSet().size() == 0) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            TaskStatus newStatus = null;
            for (int id : epic.getSubtasksIdSet()) {
                TaskStatus subtaskStatus = subtasks.get(id).getStatus();
                if (newStatus == null) { // Блок для присвоения переменной статуса первой подзадачи
                    newStatus = subtaskStatus;
                }
                if (subtaskStatus == TaskStatus.IN_PROGRESS) {  // Если какая-либо подзадача IN_PROGRESS,
                    newStatus = TaskStatus.IN_PROGRESS;         // то эпик автоматически IN_PROGRESS
                    break;
                } else if (subtaskStatus != newStatus) {        // Если подзадачи имеют разные статусы,
                    newStatus = TaskStatus.IN_PROGRESS;          // то эпик автоматически IN_PROGRESS
                    break;
                }
            }
            epic.setStatus(newStatus); // Если статус всех подзадач был одинаков, присваиваем его эпику
        }
    }
}