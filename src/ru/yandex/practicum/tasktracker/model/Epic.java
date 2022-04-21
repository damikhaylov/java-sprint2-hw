package ru.yandex.practicum.tasktracker.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Epic extends Task {
    private TaskStatus status;
    final private Map<Integer, Subtask> subtasks;

    public Epic(int id, String name, String description) {
        super(id, name, TaskStatus.NEW, description);
        subtasks = new HashMap<>();
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Возвращает все подзадачи эпика в виде HashMap
     *
     * @return HashMap c объектами Subtask в качестве значений и int идентификаторами подзадач в качестве ключей
     */
    public Map<Integer, Subtask> getSubtasksMap() {
        return subtasks;
    }

    /**
     * Возвращает все подзадачи эпика в виде ArrayList
     *
     * @return ArrayList c объектами Subtask
     */
    public ArrayList<Subtask> getSubtasksList() {
        return new ArrayList<>(subtasks.values());
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
        result.append("subtasks=").append(getSubtasksMap().values());
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
                && Objects.equals(getSubtasksMap(), epic.getSubtasksMap());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + (getStatus() != null ? getStatus().hashCode() : 0);
        hash = 31 * hash + (getSubtasksMap() != null ? getSubtasksMap().hashCode() : 0);
        return hash;
    }
}