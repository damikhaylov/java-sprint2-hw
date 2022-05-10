package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.exeption.ManagerLoadException;
import ru.yandex.practicum.tasktracker.exeption.ManagerSaveException;
import ru.yandex.practicum.tasktracker.model.*;
import ru.yandex.practicum.tasktracker.test.TestScenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    private static final String FILE_NAME = "tasks.csv";
    private static final String CSV_HEAD = "id,type,name,status,description,epic";
    private static final int TASK_FIELDS_COUNT = 6;
    private static final int DATA_FILE_MIN_LINES_COUNT = 4;
    private static final int DATA_FILE_HISTORY_LINES_COUNT = 2;

    public static void main(String[] args) {
        FileBackedTasksManager taskManager = new FileBackedTasksManager();
        TestScenario test = new TestScenario(taskManager);
        test.Add2Tasks2Epics3Subtasks();
        test.View2Tasks1Epic();
        taskManager.save();

        FileBackedTasksManager newTaskManager = loadFromFile(new File(FILE_NAME));
        System.out.println(newTaskManager.getCSVForAllTasks());
    }

    public void save() {
        StringBuilder stringBuilder = new StringBuilder(getCSVForAllTasks());
        stringBuilder.append("\n");
        stringBuilder.append(toString(this.getHistoryManager()));
        try (Writer fileWriter = new FileWriter(FILE_NAME)) {
            fileWriter.write(stringBuilder.toString());
        } catch (IOException exception) {
            throw new ManagerSaveException(String.format("Ошибка записи в файл %s.", FILE_NAME));
        }
    }

    private String getCSVForAllTasks() {
        List<Task> tasks = new ArrayList<>(getTasks());
        tasks.addAll(getEpics());
        tasks.addAll(getSubtasks());
        tasks.sort(Comparator.comparing(Task::getId));
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

    Task fromString(String value) {
        Task task;
        int id;
        TaskType type;
        TaskStatus status;
        int epicId;
        String[] fields = value.split(",", -1);
        if (fields.length != TASK_FIELDS_COUNT) {
            throw new ManagerLoadException(
                    String.format("Некорректное число полей данных задачи ( = %d) в строке:%n%s",
                            fields.length, value));
        }
        try {
            id = Integer.parseInt(fields[0]);
            type = TaskType.valueOf(fields[1]);
            status = TaskStatus.valueOf(fields[3]);
            epicId = (type == TaskType.SUBTASK) ? Integer.parseInt(fields[5]) : 0;
        } catch (IllegalArgumentException exception) {
            throw new ManagerLoadException(String.format("Ошибка в формате данных в строке:%n%s", value));
        }
        String name = fields[2];
        String description = fields[4];
        if (type == TaskType.EPIC) {
            return new Epic(id, name, status, description);
        } else if (type == TaskType.SUBTASK) {
            Epic epic = this.getEpic(epicId);
            if (epic == null) {
                throw new ManagerLoadException(
                        String.format("Подзадача ссылается на несуществующий эпик в строке:%n%s", value));
            }
            return new Subtask(id, name, status, description, epic);
        } else {
            return new Task(id, name, status, description);
        }
    }

    static List<Integer> historyFromString(String value) {
        List<Integer> historyTasksId = new ArrayList<>();
        String[] history = value.split(",");
        for (String id : history) {
            try {
                historyTasksId.add(Integer.valueOf(id));
            } catch (NumberFormatException exception) {
                throw new ManagerLoadException(
                        String.format("Идентификатор задачи в истории просмотров - не целое число: %s", id));
            }
        }
        return historyTasksId;
    }

    static public FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager taskManager = new FileBackedTasksManager();
        String csv = null;
        try {
            csv = Files.readString(file.toPath());
        } catch (IOException exception) {
            throw new ManagerLoadException(String.format("Ошибка чтения файла %s.", file.toPath()));
        }
        String[] lines = csv.split("\\n");
        if (lines.length >= DATA_FILE_MIN_LINES_COUNT) {
            for (int i = 1; i < lines.length - DATA_FILE_HISTORY_LINES_COUNT; i++) {
                taskManager.addTaskOfAnyType(taskManager.fromString(lines[i]));
            }
        }
        return taskManager;
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