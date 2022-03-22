package ru.yandex.practicum.tasktracker.model;
import ru.yandex.practicum.tasktracker.manager.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Epic extends Task {
    // Сокрытие переменной status суперкласса применяется, чтобы работать со статусом эпика,
    // не открывая доступ к установке значения статуса пользователю кроме как через конструктор задач и подзадач
    private TaskStatus status;
    final private HashMap<Integer, Subtask> subtasks;

    public Epic(int id, String name, String description) {
        super(id, name, TaskStatus.NEW, description);
        subtasks = new HashMap<>();
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Обновляет статус эпика на основе перебора статусов его подзадач. Логика вынесена в статический метод
     * getEpicStatusBySubtasks класса TaskManager, но сам метод принадлежит классу Epic, чтобы не открывать вовне доступ
     * к установке значения статуса эпика.
     */
    public void renewStatus() {
        status = TaskManager.getEpicStatusBySubtasks(subtasks);
    }

    /**
     * Возвращает все подзадачи эпика в виде ArrayList
     *
     * @return ArrayList c объектами Subtask
     */
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        status = TaskStatus.NEW; // Для эпика без подзадач статус NEW
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.getOrDefault(id, null);
    }

    /**
     * Добавляет новую подзадачу в эпик, если она не null и если getEpic() подзадачи совпадает с текущим эпиком
     *
     * @param subtask объект Subtask
     * @return id подзадачи, если она добавлена и 0, если не добавлена
     */
    public int addSubtask(Subtask subtask) {
        if (subtask != null && subtask.getEpic().equals(this)) {
            subtasks.put(subtask.getId(), subtask);
            renewStatus(); // Вызывается обновление статуса эпика в связи с изменением состава подзадач
            return subtask.getId();
        } else {
            return 0;
        }
    }

    /**
     * Заменяет подзадачу в эпике, если новая подзадача не null, если она передаётся с id существующей подзадачи
     * и если getEpic() подзадачи совпадает с текущим эпиком
     *
     * @param subtask объект Subtask
     * @return true, если подзадача добавлена, false, если нет
     */
    public boolean replaceSubtask(Subtask subtask) {
        if (subtask != null && subtask.getEpic() == this && subtasks.containsKey(subtask.getId())) {
            subtasks.replace(subtask.getId(), subtask);
            renewStatus(); // Вызывается обновление статуса эпика в связи с возможным изменением статуса подзадачи
            return true;
        } else {
            return false;
        }
    }

    /**
     * Удаляет подзадачу с заданным id
     *
     * @param id идентификатор подзадачи
     * @return true, если подзадача удалена, false, если подзадача с заданным id отсутствует
     */
    public boolean deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            subtasks.remove(id);
            renewStatus(); // Вызывается обновление статуса эпика в связи с изменением состава подзадач
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Epic{");
        result.append("id=").append(getId()).append(", ");
        result.append("name='").append(getName()).append("', ");
        result.append("status=").append(getStatus()).append(", ");
        if (getDescription() != null) {
            result.append("description.length=").append(getDescription().length()).append(", ");
        } else {
            result.append("description=null, ");
        }
        result.append("subtasks=").append(subtasks.values());
        result.append('}');
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(getStatus(), epic.getStatus())
                && Objects.equals(getSubtasks(), epic.getSubtasks());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + (getStatus() != null ? getStatus().hashCode() : 0);
        hash = 31 * hash + (getSubtasks() != null ? getSubtasks().hashCode() : 0);
        return hash;
    }
}