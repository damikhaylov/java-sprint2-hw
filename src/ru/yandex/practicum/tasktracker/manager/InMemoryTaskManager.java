package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int nextTaskId; // очередной (ещё не присвоенный) id задачи
    final protected Map<Integer, Task> tasks;
    final protected Map<Integer, Epic> epics;
    final protected Map<Integer, Subtask> subtasks;
    final protected TreeSet<Task> prioritizedTasks;
    final protected HistoryManager historyManager;

    public InMemoryTaskManager() {
        nextTaskId = 1; // нумерация задач будет начинаться с 1
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(
                Task::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ).thenComparing(Task::getId));
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

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int id) {
        Epic epic = getEpic(id);
        if (epic == null) {
            return null;
        }
        return new ArrayList<>(epic.getSubtasksMap().values());
    }

    /**
     * Удаляет все задачи
     */
    @Override
    public void removeAllTasks() {
        removeTasksFromHistoryByIDSet(tasks.keySet()); // удаление из истории просмотров
        removeAnyTypeTaskCollectionFromPrioritizedTasks(tasks.values());
        tasks.clear();
    }

    /**
     * Удаляет все эпики (а также все подзадачи вместе с ними)
     */
    @Override
    public void removeAllEpics() {
        removeTasksFromHistoryByIDSet(subtasks.keySet()); // удаление всех подзадач из истории просмотров
        removeTasksFromHistoryByIDSet(epics.keySet()); // удаление всех эпиков из истории просмотров
        removeAnyTypeTaskCollectionFromPrioritizedTasks(subtasks.values());
        subtasks.clear();
        epics.clear();
    }

    /**
     * Удаляет все подзадачи
     */
    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasksMap().clear();
            Epic updatedEpic = EpicPropertiesHelper.calculateAndSet(epic);
            epics.replace(updatedEpic.getId(), updatedEpic);
        }
        removeTasksFromHistoryByIDSet(subtasks.keySet()); // удаление всех подзадач из истории просмотров
        removeAnyTypeTaskCollectionFromPrioritizedTasks(subtasks.values());
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
        return getAnyTypeTask(id, tasks);
    }

    /**
     * Возвращает эпик по id, или null, если эпика с таким id не существует
     */
    @Override
    public Epic getEpic(int id) {
        return getAnyTypeTask(id, epics);
    }

    /**
     * Возвращает подзадачу по id, или null, если задачи с таким id не найдено
     */
    @Override
    public Subtask getSubtask(int id) {
        return getAnyTypeTask(id, subtasks);
    }

    /**
     * Добавляет новую задачу, эпик или подзадачу в соответствующие их типу коллекции, если переданный объект
     * не содержит некорректных параметров
     *
     * @param task объект класса Task или классов-наследников Epic и Subtask
     * @return id подзадачи, если она добавлена и 0, если не добавлена
     */
    @Override
    public int addTaskOfAnyType(Task task) {
        if (task == null) {
            return 0;
        }
        // Если задача передана с дефолтным id, он будет заменён на очередной
        if (task.getId() == Task.DEFAULT_ID) {
            task = TasksHelper.replaceTaskId(task, nextTaskId);
        }
        // Если задача передана с неположительным id, задача не будет добавлена
        if (task.getId() <= 0) {
            return 0;
        }
        // Если задача передана с уже занятым id, задача не будет добавлена
        if (tasks.containsKey(task.getId()) || epics.containsKey(task.getId()) || subtasks.containsKey(task.getId())) {
            return 0;
        }
        if (task.getId() >= nextTaskId) {
            nextTaskId = task.getId() + 1;
        }

        if (task.getClass() == Epic.class) {
            Epic epic = (Epic) task;
            epic = EpicPropertiesHelper.calculateAndSet(epic);
            epic.getSubtasksMap().clear(); // очищается таблица подзадач, сформированный в обход менеджера
            epics.put(epic.getId(), epic);
        } else if (task.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) task;
            Epic epic = epics.get(subtask.getEpicId());
            if (epic == null) {
                return 0;
            }
            if (!epics.containsKey(epic.getId())) { // Если эпик переданной подзадачи ещё не внесён в менеджер
                return 0;
            }
            if (isTaskTimeOverlappingAnother(subtask)) {
                return 0;
            }
            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);
            epic.getSubtasksMap().put(subtask.getId(), subtask);
            Epic updatedEpic = EpicPropertiesHelper.calculateAndSet(epic);
            epics.replace(updatedEpic.getId(), updatedEpic);
        } else {
            if (isTaskTimeOverlappingAnother(task)) {
                return 0;
            }
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
        }
        return task.getId();
    }

    /**
     * Заменяет задачу, если новая задача не null, и если она передаётся с id существующей задачи
     *
     * @param task объект Task
     * @return true, если подзадача добавлена, false, если нет
     */
    @Override
    public boolean replaceTask(Task task) {
        if (task == null) {
            return false;
        }
        if (!tasks.containsKey(task.getId())) {
            return false;
        }
        if (isTaskTimeOverlappingAnother(task)) {
            return false;
        }
        Task originTask = tasks.get(task.getId());
        tasks.replace(task.getId(), task);
        prioritizedTasks.remove(originTask);
        prioritizedTasks.add(task);
        return true;
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
            epic.getSubtasksMap().clear();
            epic.getSubtasksMap().putAll(epics.get(epic.getId()).getSubtasksMap());
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
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return false;
        }
        if (!epic.getSubtasksMap().containsKey(subtask.getId())) {
            return false;
        }
        if (isTaskTimeOverlappingAnother(subtask)) {
            return false;
        }
        Subtask originSubtask = subtasks.get(subtask.getId());
        subtasks.replace(subtask.getId(), subtask); // замена в таблице подзадач
        epic.getSubtasksMap().replace(subtask.getId(), subtask); // замена в таблице подзадач эпика
        prioritizedTasks.remove(originSubtask);
        prioritizedTasks.add(subtask);
        Epic updatedEpic = EpicPropertiesHelper.calculateAndSet(epic);
        epics.replace(updatedEpic.getId(), updatedEpic);
        return true;
    }

    /**
     * Удаляет задачу, эпик, подзадачу с заданным id из коллекции, соответствующей их типу
     *
     * @param id id объекта (задачи, эпика, подзадачи)
     */
    @Override
    public boolean removeTaskOfAnyTypeById(int id) {
        if (tasks.containsKey(id)) {
            prioritizedTasks.remove(tasks.get(id));
            historyManager.remove(id);
            tasks.remove(id);
            return true;
        }
        if (epics.containsKey(id)) {
            // удаление всех подзадач эпика из истории просмотра
            removeTasksFromHistoryByIDSet(epics.get(id).getSubtasksMap().keySet());
            // удаление всех подзадач эпика из таблицы подзадач
            for (Integer subtaskId : epics.get(id).getSubtasksMap().keySet()) {
                prioritizedTasks.remove(subtasks.get(subtaskId));
                subtasks.remove(subtaskId);
            }
            historyManager.remove(id);
            epics.remove(id); // удаление эпика
            return true;
        }
        if (subtasks.containsKey(id)) {
            Epic epic = epics.get(subtasks.get(id).getEpicId());
            epic.getSubtasksMap().remove(id); // удаление подзадачи из таблицы подзадач эпика
            prioritizedTasks.remove(subtasks.get(id));
            historyManager.remove(id);
            subtasks.remove(id); // удаление подзадачи из таблицы подзадач
            Epic updatedEpic = EpicPropertiesHelper.calculateAndSet(epic);
            epics.replace(updatedEpic.getId(), updatedEpic);
            return true;
        }
        return false;
    }

    /**
     * Возвращает историю просмотров задач, эпиков подзадач
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected <T extends Task> T getAnyTypeTask(int id, Map<Integer, T> tasks) {
        T task = tasks.getOrDefault(id, null);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    private <T extends Task> void removeAnyTypeTaskCollectionFromPrioritizedTasks(Collection<T> tasks) {
        for (Task task : tasks) {
            prioritizedTasks.remove(task);
        }
    }

    protected boolean isTaskTimeOverlappingAnother(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }

        Task floorPriorityTask = prioritizedTasks.floor(task);
        if (floorPriorityTask != null && floorPriorityTask.getId() == task.getId()) {
            floorPriorityTask = prioritizedTasks.lower(floorPriorityTask);
        }

        Task ceilingPriorityTask = prioritizedTasks.ceiling(task);
        if (ceilingPriorityTask != null && ceilingPriorityTask.getId() == task.getId()) {
            ceilingPriorityTask = prioritizedTasks.higher(ceilingPriorityTask);
        }

        if (floorPriorityTask != null
                && floorPriorityTask.getEndTime().isAfter(task.getStartTime())
        ) {
            return true;
        }

        if (ceilingPriorityTask != null
                && ceilingPriorityTask.getStartTime() != null
                && ceilingPriorityTask.getStartTime().isBefore(task.getEndTime())
        ) {
            return true;
        }

        return false;
    }
}