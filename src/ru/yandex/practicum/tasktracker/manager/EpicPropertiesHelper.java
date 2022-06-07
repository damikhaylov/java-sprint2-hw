package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.Map;

public class EpicPropertiesHelper {

    protected static Epic calculateAndSet(Epic epic) {
        Epic updatedEpic1 = setEpicStatusBySubtasks(epic);
        Epic updatedEpic2 = setEpicTimesBySubtasks(updatedEpic1);
        return updatedEpic2;
    }

    /**
     * Назначает статус эпика на основе статусов его подзадач
     */
    private static Epic setEpicStatusBySubtasks(Epic epic) {
        Epic updatedEpic;
        if (epic.getSubtasksMap().size() == 0) {
            //epic.setStatus(TaskStatus.NEW);
            return new Epic(epic.getId(), epic.getName(), TaskStatus.NEW, epic.getDescription(),
                    epic.getStartTime(), epic.getDuration());
        }
        TaskStatus newStatus = null;
        for (Subtask subtask : epic.getSubtasksMap().values()) {
            TaskStatus subtaskStatus = subtask.getStatus();
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
        //epic.setStatus(newStatus); // Если статус всех подзадач был одинаков, присваиваем его эпику
        updatedEpic = new Epic(epic.getId(), epic.getName(), newStatus, epic.getDescription(),
                epic.getStartTime(), epic.getDuration());
        updatedEpic.getSubtasksMap().putAll(epic.getSubtasksMap());
        return updatedEpic;
    }

    /**
     * Назначает время начала, окончания и продолжительность эпика на основе значений для подзадач
     */
    private static Epic setEpicTimesBySubtasks(Epic epic) {
        Epic updatedEpic;
        if (epic.getSubtasksMap().size() == 0) {
            updatedEpic = new Epic(epic.getId(), epic.getName(), epic.getStatus(), epic.getDescription(),
                    null, 0);
            updatedEpic.setEndTime(null);
//            epic.setStartTime(null);
//            epic.setDuration(0);
//            epic.setEndTime(null);
            return updatedEpic;
        }

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        int duration = 0;
        for (Subtask subtask : epic.getSubtasksMap().values()) {
            LocalDateTime subtaskStartTime = subtask.getStartTime();
            LocalDateTime subtaskEndTime = subtask.getEndTime();
            int subtaskDuration = subtask.getDuration();

            duration += subtaskDuration;

            if (subtaskStartTime == null) {
                continue;
            }

            if (startTime == null) {
                startTime = subtaskStartTime;
                endTime = subtaskEndTime;
                continue;
            }

            if (subtaskStartTime.isBefore(startTime)) {
                startTime = subtaskStartTime;
            }
            if (subtaskEndTime.isAfter(endTime)) {
                endTime = subtaskEndTime;
            }
        }
        updatedEpic = new Epic(epic.getId(), epic.getName(), epic.getStatus(), epic.getDescription(),
                startTime, duration);
        updatedEpic.setEndTime(endTime);
        updatedEpic.getSubtasksMap().putAll(epic.getSubtasksMap());
        return updatedEpic;

//        epic.setStartTime(startTime);
//        epic.setDuration(duration);
//        epic.setEndTime(endTime);
    }
}
