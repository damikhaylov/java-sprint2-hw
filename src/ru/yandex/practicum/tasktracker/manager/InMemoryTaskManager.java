package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager{
    private int nextTaskId; // очередной (ещё не присвоенный) id задачи
    final private HashMap<Integer, Task> tasks;
    final private HashMap<Integer, Epic> epics;

    public InMemoryTaskManager() {
        nextTaskId = 1; // нумерация задач будет начинаться с 1
        tasks = new HashMap<>();
        epics = new HashMap<>();
    }

    /**
     * Возвращает очередной номер, который может быть присвоен новой задаче в качестве id
     */
    public int getNextTaskId() {
        return nextTaskId;
    }

    /**
     * Возвращает все задачи в виде ArrayList c объектами Task
     */
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Возвращает все эпики в виде ArrayList c объектами Epic
     */
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    /**
     * Возвращает все подзадачи в виде ArrayList c объектами Subtask, собирая их из всех эпиков
     */
    public ArrayList<Subtask> getSubtasks() {
        ArrayList<Subtask> allSubtasks = new ArrayList<>();
        for (Integer key : epics.keySet()) {
            allSubtasks.addAll(epics.get(key).getSubtasksList());
        }
        return allSubtasks;
    }

    /**
     * Удаляет все задачи
     */
    public void deleteAllTasks() {
        tasks.clear();
    }

    /**
     * Удаляет все эпики
     */
    public void deleteAllEpics() {
        epics.clear();
    }

    /**
     * Удаляет все подзадачи во всех эпиках
     */
    public void deleteAllSubtasks() {
        for (Integer key : epics.keySet()) {
            Epic epic = epics.get(key);
            epic.getSubtasksMap().clear();
            epic.renewStatus();
        }
    }

    /**
     * Возвращает задачу по id, или null, если задачи с таким id не существует
     */
    public Task getTask(int id) {
        return tasks.getOrDefault(id, null);
    }

    /**
     * Возвращает эпик по id, или null, если эпика с таким id не существует
     */
    public Epic getEpic(int id) {
        return epics.getOrDefault(id, null);
    }

    /**
     * Возвращает подзадачу по id, проводя поиск во всех эпиках или null, если задачи с таким id не найдено
     */
    public Subtask getSubtask(int id) {
        for (Integer key : epics.keySet()) {
            Subtask desiredSubtask = epics.get(key).getSubtasksMap().getOrDefault(id, null);
            if (desiredSubtask != null) {
                return desiredSubtask;
            }
        }
        return null;
    }

    /**
     * Добавляет новую задачу, эпик или подзадачу в соответствующие их типу коллекции, если переданный объект не null
     * и если id переданного объекта соответствует очередному номеру, хранящемуся в nextTaskId
     *
     * @param task объект класса Task или классов-наследников Epic и Subtask
     * @return id подзадачи, если она добавлена и 0, если не добавлена
     */
    public int addTaskOfAnyType(Task task) {
        if (task != null && task.getId() == nextTaskId) {
            nextTaskId++;
            if (task.getClass() == Epic.class) {
                epics.put(task.getId(), (Epic) task);
                return task.getId();
            } else if (task.getClass() == Subtask.class) {
                Subtask subtask = (Subtask) task;
                subtask.getEpic().getSubtasksMap().put(subtask.getId(), subtask);
                subtask.getEpic().renewStatus();
                return subtask.getId();
            } else {
                tasks.put(task.getId(), task);
                return task.getId();
            }
        }
        return 0;
    }

    /**
     * Заменяет задачу, если новая задача не null, и если она передаётся с id существующей задачи
     *
     * @param task объект Task
     * @return true, если подзадача добавлена, false, если нет
     */
    public boolean replaceTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            tasks.replace(task.getId(), task);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Заменяет эпик, если новый эпик не null, и если он передаётся с id существующего эпика
     *
     * @param epic объект Epic
     * @return true, если эпик добавлен, false, если нет
     */
    public boolean replaceEpic(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId())) {
            epics.replace(epic.getId(), epic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Заменяет подзадачу, если новая подзадача не null, если она передаётся с id существующей подзадачи
     *
     * @param subtask объект Subtask
     * @return true, если подзадача добавлена, false, если нет
     */
    public boolean replaceSubtask(Subtask subtask) {
        if (subtask == null) {
            return false;
        }
        Epic epic = subtask.getEpic();
        if (epic.getSubtasksMap().containsKey(subtask.getId())) {
            epic.getSubtasksMap().replace(subtask.getId(), subtask);
            epic.renewStatus(); // Вызывается обновление статуса эпика в связи с возможным изменением статуса подзадачи
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
    public void deleteTaskOfAnyTypeById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        } else if (epics.containsKey(id)) {
            epics.remove(id);
        } else {
            for (Integer key : epics.keySet()) {
                Epic epic = epics.get(key);
                if (epic.getSubtasksMap().containsKey(id)) {
                    epic.getSubtasksMap().remove(id);
                    epic.renewStatus(); // Вызывается обновление статуса эпика в связи с изменением состава подзадач
                    return;
                }
            }
        }
    }

    /**
     * Возвращает статус для эпика, который содержал бы переданную в качестве аргумента коллекцию подзадач
     *
     * @param subtasks HashMap, содержащий подзадачи
     * @return TaskStatus статус эпика, который содержал бы переданную в качестве аргумента коллекцию подзадач
     */
    public static TaskStatus getEpicStatusBySubtasks(HashMap<Integer, Subtask> subtasks) {
        if (subtasks.size() == 0) {
            return TaskStatus.NEW;
        } else {
            TaskStatus newStatus = null;
            for (Integer key : subtasks.keySet()) {
                TaskStatus subtaskStatus = subtasks.get(key).getStatus();
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
            return newStatus; // Если статус всех подзадач был одинаков, присваиваем его эпику
        }
    }
}