package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.test.TestScenario;

import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    private static final String FILE_NAME = "tasks.csv";
    private static final String CSV_HEAD = "id,type,name,status,description,epic";

    public static void main(String[] args) {
        FileBackedTasksManager taskManager = new FileBackedTasksManager();
        TestScenario test = new TestScenario(taskManager);
        test.Add2Tasks2Epics3Subtasks();
        test.View2Tasks1Epic();
        taskManager.save();
    }

    public void save() {
        StringBuilder stringBuilder = new StringBuilder(getCSVForAllTasks());
        stringBuilder.append("\n");
        stringBuilder.append(toString(this.getHistoryManager()));
        try (Writer fileWriter = new FileWriter(FILE_NAME)) {
            fileWriter.write(stringBuilder.toString());
        } catch (IOException exception) {
        }
    }

    private String getCSVForAllTasks() {
        List<Task> tasks = new ArrayList<>(getTasks());
        tasks.addAll(getEpics());
        tasks.addAll(getSubtasks());
        StringBuilder stringBuilder = new StringBuilder(CSV_HEAD).append("\n");
        for (Task task : tasks) {
            stringBuilder.append(toString(task)).append("\n");
        }
        return stringBuilder.toString();
    }

    static private String toString(HistoryManager manager) {
        return manager.getHistory().stream().map(x -> String.valueOf(x.getId()))
                .collect(Collectors.joining(","));
    }

    static public String toString(Task task) {
        return String.format("%d,%S,%s,%s,%s,%s",
                task.getId(),
                task.getClass().getSimpleName(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                ((task instanceof Subtask) ? ((Subtask) task).getEpic().getId() : "")
        );
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public int addTaskOfAnyType(Task task) {
        int id = super.addTaskOfAnyType(task);
        save();
        return id;
    }

    @Override
    public boolean replaceTask(Task task) {
        boolean isSuccessfullyReplacing = super.replaceTask(task);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public boolean replaceEpic(Epic epic) {
        boolean isSuccessfullyReplacing = super.replaceEpic(epic);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public boolean replaceSubtask(Subtask subtask) {
        boolean isSuccessfullyReplacing = super.replaceSubtask(subtask);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public void removeTaskOfAnyTypeById(int id) {
        super.removeTaskOfAnyTypeById(id);
        save();
    }


}