package ru.yandex.practicum.tasktracker.model;

import java.util.Objects;

public class Subtask extends Task {
    // TODO: Комментарий для ревью (удалить после спринта 6) - В класс добавлены конструкторы для создания задач без id
    //  (как в примере тестов техзадания)

    final private Epic epic;

    public Subtask(int id, String name, TaskStatus status, String description, Epic epic) {
        super(id, name, status, description);
        this.epic = epic;
    }

    public Subtask(String name, TaskStatus status, String description, Epic epic) {
        super(name, status, description);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Subtask{");
        result.append("id=").append(getId()).append(", ");
        result.append("name='").append(getName()).append("', ");
        result.append("status=").append(getStatus()).append(", ");
        if (getDescription() != null) {
            result.append("description.length=").append(getDescription().length()).append(", ");
        } else {
            result.append("description=null, ");
        }
        result.append("epic.id=").append(epic.getId());
        result.append('}');
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return Objects.equals(getEpic(), subtask.getEpic());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + (getEpic() != null ? getEpic().hashCode() : 0);
        return hash;
    }
}