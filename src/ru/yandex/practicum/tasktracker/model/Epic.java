package ru.yandex.practicum.tasktracker.model;

import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {

    private final LocalDateTime endTime;
    final private Map<Integer, Subtask> subtasksMap;

    public Epic(int id, String name, TaskStatus status, String description,
                LocalDateTime startTime, int duration, LocalDateTime endTime) {
        super(id, name, status, description, startTime, duration);
        subtasksMap = new HashMap<>();
        this.endTime = endTime;
    }

    public Epic(int id, String name, String description) {
        this(id, name, TaskStatus.NEW, description, null, 0, null);
    }

    public Epic(String name, String description) {
        this(DEFAULT_ID, name, TaskStatus.NEW, description, null, 0, null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Map<Integer, Subtask> getSubtasksMap() {
        return subtasksMap;
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
        if (getStartTime() != null) {
            result.append("startTime=").append(getStartTime().toString()).append(", ");
        } else {
            result.append("startTime=null").append(", ");
        }
        result.append("duration=").append(getDuration()).append(", ");
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
                && Objects.equals(getSubtasksMap().keySet(), epic.getSubtasksMap().keySet());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + (getStatus() != null ? getStatus().hashCode() : 0);
        hash = 31 * hash + (getSubtasksMap() != null ? getSubtasksMap().keySet().hashCode() : 0);
        return hash;
    }
}