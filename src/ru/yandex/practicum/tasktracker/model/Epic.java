package ru.yandex.practicum.tasktracker.model;

import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {

    private TaskStatus status;
    private LocalDateTime startTime;
    private int duration;
    private LocalDateTime endTime;
    final private Set<Integer> subtasksIdSet; // множество, содержащее идентификаторы подзадач

    public Epic(int id, String name, TaskStatus status, String description, LocalDateTime startTime, int duration) {
        super(id, name, description);
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
        subtasksIdSet = new HashSet<>();
    }

    public Epic(int id, String name, String description) {
        this(id, name, TaskStatus.NEW, description,
                null, 0);
    }

    public Epic(String name, TaskStatus status, String description) {
        this(DEFAULT_ID, name, status, description, null, 0);
    }

    public Epic(String name, String description) {
        this(name, TaskStatus.NEW, description);
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Возвращает идентификаторы всех подзадач эпика в виде HashSet
     *
     * @return HashSet c int идентификаторами подзадач
     */
    public Set<Integer> getSubtasksIdSet() {
        return subtasksIdSet;
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
        result.append("subtasks=").append(getSubtasksIdSet());
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
                && Objects.equals(getSubtasksIdSet(), epic.getSubtasksIdSet());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + (getStatus() != null ? getStatus().hashCode() : 0);
        hash = 31 * hash + (getSubtasksIdSet() != null ? getSubtasksIdSet().hashCode() : 0);
        return hash;
    }
}