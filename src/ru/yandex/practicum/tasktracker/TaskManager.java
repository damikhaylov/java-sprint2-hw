package ru.yandex.practicum.tasktracker;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int nextTaskId; // очередной (ещё не присвоенный) id задачи
    final private HashMap<Integer, Task> tasks;
    final private HashMap<Integer, Epic> epics;

    public TaskManager() {
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
            allSubtasks.addAll(epics.get(key).getSubtasks());
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
            epics.get(key).deleteAllSubtasks();
        }
    }

    /**
     * Возвращает задачу по id, или null, если задачи с таким id не существует
     */
    public Task getTaskById(int id) {
        return tasks.getOrDefault(id, null);
    }

    /**
     * Возвращает эпик по id, или null, если эпика с таким id не существует
     */
    public Epic getEpicById(int id) {
        return epics.getOrDefault(id, null);
    }

    /**
     * Возвращает подзадачу по id, проводя поиск во всех эпиках или null, если задачи с таким id не найдено
     */
    public Subtask getSubtaskById(int id) {
        for (Integer key : epics.keySet()) {
            Subtask desiredSubtask = epics.get(key).getSubtaskById(id);
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
                return subtask.getEpic().addSubtask(subtask);
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
        return subtask.getEpic().replaceSubtask(subtask);
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
            for (Integer i : epics.keySet()) {
                boolean isDeletingFromCurrentEpic = epics.get(i).deleteSubtaskById(id);
                if (isDeletingFromCurrentEpic) {
                    return;
                }
            }
        }
    }
}
