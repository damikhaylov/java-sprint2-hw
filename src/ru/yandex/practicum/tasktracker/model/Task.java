package ru.yandex.practicum.tasktracker.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    public static final int DEFAULT_ID = 0;
    private final int id;
    private final TaskStatus status;
    private final String name;
    private final String description;
    private final LocalDateTime startTime;
    private final int duration;

    public Task(int id, String name, TaskStatus status, String description, LocalDateTime startTime, int duration) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.description = description;
        this.startTime = startTime;
        this.duration = Math.max(duration, 0);
    }

    public Task(String name, TaskStatus status, String description, LocalDateTime startTime, int duration) {
        this(DEFAULT_ID, name, status, description, startTime, duration);
    }

    protected Task(int id, String name, String description) {
        this(id, name, TaskStatus.NEW, description, null, 0);
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        return (startTime != null) ? startTime.plusMinutes(duration) : null;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Task{");
        result.append("id=").append(getId()).append(", ");
        result.append("name='").append(getName()).append("', ");
        result.append("status=").append(getStatus()).append(", ");
        if (getDescription() != null) {
            result.append("description.length=").append(getDescription().length()).append(", ");
        } else {
            result.append("description=null").append(", ");
        }
        if (getStartTime() != null) {
            result.append("startTime=").append(getStartTime().toString()).append(", ");
        } else {
            result.append("startTime=null").append(", ");
        }
        result.append("duration=").append(getDuration());
        result.append('}');
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return getId() == task.getId()
                && Objects.equals(getStatus(), task.getStatus())
                && Objects.equals(getName(), task.getName())
                && Objects.equals(getDescription(), task.getDescription())
                && Objects.equals(getStartTime(), task.getStartTime())
                && getDuration() == task.getDuration();
    }

    @Override
    public int hashCode() {
        int hash = getId();
        hash = 31 * hash + (getStatus() != null ? getStatus().hashCode() : 0);
        hash = 31 * hash + (getName() != null ? getName().hashCode() : 0);
        hash = 31 * hash + (getDescription() != null ? getDescription().hashCode() : 0);
        hash = 31 * hash + (getStartTime() != null ? getStartTime().hashCode() : 0);
        hash = 31 * hash + getDuration();
        return hash;
    }
}