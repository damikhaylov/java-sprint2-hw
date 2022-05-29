package ru.yandex.practicum.tasktracker.model;

import java.util.Objects;

public class Task {
    // TODO: Комментарий для ревью (удалить после спринта 6) - В класс добавлены конструкторы для создания задач без id
    //  (как в примере тестов техзадания)

    public static final int DEFAULT_ID = 0;
    private final int id;
    private final TaskStatus status;
    private final String name;
    private final String description;

    public Task(int id, String name, TaskStatus status, String description) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.description = description;
    }

    public Task(String name, TaskStatus status, String description) {
        this(DEFAULT_ID, name, status, description);
    }

    public Task(int id, String name, String description) {
        this(id, name, TaskStatus.NEW, description);
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Task{");
        result.append("id=").append(getId()).append(", ");
        result.append("name='").append(getName()).append("', ");
        result.append("status=").append(getStatus()).append(", ");
        if (getDescription() != null) {
            result.append("description.length=").append(getDescription().length());
        } else {
            result.append("description=null");
        }
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
                && Objects.equals(getDescription(), task.getDescription());
    }

    @Override
    public int hashCode() {
        int hash = getId();
        hash = 31 * hash + (getStatus() != null ? getStatus().hashCode() : 0);
        hash = 31 * hash + (getName() != null ? getName().hashCode() : 0);
        hash = 31 * hash + (getDescription() != null ? getDescription().hashCode() : 0);
        return hash;
    }
}