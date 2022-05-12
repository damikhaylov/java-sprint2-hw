package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.exeption.*;
import ru.yandex.practicum.tasktracker.model.*;
import ru.yandex.practicum.tasktracker.test.TestScenario;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;

import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private static final String CSV_HEAD = "id,type,name,status,description,epic";
    private static final int TASK_FIELDS_COUNT = 6;

    // Минимальное число строк в файле csv сокращено до двух: заголовок и строка данных задачи. Ранее обязательные
    // строки разделителя и истории (которая могла быть пустой) теперь могут отсутствовать в файле.
    private static final int DATA_FILE_MIN_LINES_COUNT = 2;

    private final File file;

    FileBackedTasksManager(File file) {
        this.file = file;
    }

    public static void main(String[] args) {
        // Создание первого менеджера для добавления данных
        TaskManager taskManager = Managers.getDefault();
        String fileName = Managers.DEFAULT_BACKUP_FILE_NAME;
        // Использование методов специально созданного класса TestScenario для добавления тестовых данных
        TestScenario test = new TestScenario(taskManager);

        try {
            test.Add2Tasks2Epics3Subtasks(); // добавление двух задач, двух эпиков и трёх подзадач
            test.View2Tasks1Epic(); // имитация просмотра двух задач и эпика
            System.out.printf("%n>>>>> Тестовые данные были добавлены в менеджер и сохранены в файл %s%n%n", fileName);
        } catch (ManagerSaveException exception) {
            System.out.printf(">>>>> Из-за ошибок не удалось сохранить тестовые данные в файл %s%n", fileName);
            System.out.println(exception.getMessage());
            if (exception.getCause() != null) {
                exception.getCause().printStackTrace();
            }
        }

        try {
            // новый менеджер для считывания создаётся как представитель класса FileBackedTasksManager, так как в дальнейшем
            // использует методы, специфичные для этого класса
            FileBackedTasksManager newTaskManager = loadFromFile(new File(fileName));
            System.out.printf(">>>>> Тестовые данные загружены в новый менеджер из файла %s%n%n", fileName);
            System.out.println(">>>>> Список задач (подгружен из нового менеджера):");
            System.out.println(newTaskManager.getCSVForAllTasks());
            System.out.println(">>>>> История просмотров (подгружена из нового менеджера):");
            System.out.println(toString(newTaskManager.getHistoryManager()));
        } catch (ManagerLoadException exception) {
            System.out.printf(">>>>> Из-за ошибок не удалось загрузить данные из файла %s%n", fileName);
            System.out.println(exception.getMessage());
            if (exception.getCause() != null) {
                exception.getCause().printStackTrace();
            }
        }
    }

    /**
     * Метод сохраняет задачи и историю просмотров в файл
     */
    public void save() throws ManagerSaveException {
        String fileName = file.getName();
        StringBuilder stringBuilder = new StringBuilder(getCSVForAllTasks());
        stringBuilder.append("\n");
        stringBuilder.append(toString(this.getHistoryManager()));
        try (Writer fileWriter = new FileWriter(fileName, StandardCharsets.UTF_8)) {
            fileWriter.write(stringBuilder.toString());
        } catch (IOException exception) {
            throw new ManagerSaveException(String.format("Ошибка записи в файл %s.", fileName), exception);
        }
    }

    /**
     * Метод формирует и возвращает строку с данными всех задач менеджера в формате csv, первая строка содержит
     * заголовки полей
     */
    private String getCSVForAllTasks() {
        List<Task> tasks = new ArrayList<>(getTasks());
        tasks.addAll(getEpics());
        tasks.addAll(getSubtasks());
        // В примере к техзаданию список отсортирован, можно было предположить, что сортировка снимает проблемы с
        // присвоением id при вставке элементов и добавлением подзадач с ещё не существующими эпиками. Но поскольку
        // сортировка не предусмотрена текстом техзадания и увеличивает асимптотическую сложность программы, она
        // больше не применяется, при этом усложнён алгоритм присвоения id, а также предусмотрена запись эпиков в файл
        // строго раньше подзадач
        StringBuilder stringBuilder = new StringBuilder(CSV_HEAD).append("\n");
        for (Task task : tasks) {
            stringBuilder.append(toString(task)).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Метод формирует и возвращает строку с данными переданной в метод задачи (разделены запятыми)
     */
    public static String toString(Task task) {
        return String.join(",",
                String.valueOf(task.getId()),
                task.getClass().getSimpleName().toUpperCase(),
                task.getName(),
                String.valueOf(task.getStatus()),
                task.getDescription(),
                ((task instanceof Subtask) ? String.valueOf(((Subtask) task).getEpic().getId()) : "")
        );
    }

    /**
     * Метод формирует и возвращает строку с id задач из истории просмотров, разделённых запятыми
     */
    public static String toString(HistoryManager manager) {
        return manager.getHistory().stream().map(x -> String.valueOf(x.getId()))
                .collect(Collectors.joining(","));
    }

    /**
     * Метод создаёт и возвращает задачу (эпик, подзадачу) на основании данных, переданных в строке csv-формата
     */
    private Task fromString(String value) throws ManagerLoadException {
        int id;
        TaskType type;
        TaskStatus status;
        int epicId;

        String[] fields = value.split(",", -1);

        if (fields.length != TASK_FIELDS_COUNT) {
            throw new ManagerLoadException(String.format("Некорректное число полей данных задачи ( = %d) в строке:%n%s",
                    fields.length, value));
        }

        try {
            id = Integer.parseInt(fields[0]);
            type = TaskType.valueOf(fields[1]);
            status = TaskStatus.valueOf(fields[3]);
            epicId = (type == TaskType.SUBTASK) ? Integer.parseInt(fields[5]) : 0;
        } catch (IllegalArgumentException exception) {
            throw new ManagerLoadException(String.format("Ошибка в формате данных в строке:%n%s", value), exception);
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
            } else {
                // удаление запрошенного для формирования подзадачи эпика из истории просмотров
                this.getHistoryManager().remove(epicId);
            }
            return new Subtask(id, name, status, description, epic);
        } else {
            return new Task(id, name, status, description);
        }
    }

    /**
     * Метод создаёт и возвращает список с id задач из истории просмотра на основании данных, переданных
     * в строке csv-формата
     */
    private static List<Integer> historyFromString(String value) throws ManagerLoadException {
        List<Integer> historyTasksId = new ArrayList<>();
        String[] history = value.split(",");
        for (String id : history) {
            try {
                historyTasksId.add(Integer.valueOf(id));
            } catch (NumberFormatException exception) {
                throw new ManagerLoadException(
                        String.format("Идентификатор задачи в истории просмотров - не целое число: %s", id), exception);
            }
        }
        return historyTasksId;
    }

    /**
     * Метод создаёт и возвращает менеджер, заполняя его данными из файла формата csv
     */
    private static FileBackedTasksManager loadFromFile(File file) throws ManagerLoadException {
        String newFileName;
        String csv;

        try {
            // Менеджер будет создаваться с автосохранением в файл, отличный от того, из которого загружаются данные
            // (файл будет создан в той же директории, но с префиксом new)
            String directoryPath = Paths.get(file.getAbsolutePath()).getParent().toString();
            newFileName = Paths.get(directoryPath, "new" + file.getName()).toString();

            csv = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ManagerLoadException(String.format("Ошибка чтения файла %s.", file.toPath()), exception);
        }
        FileBackedTasksManager taskManager = new FileBackedTasksManager(new File(newFileName));

        String[] lines = csv.split("\\n");

        if (lines.length < DATA_FILE_MIN_LINES_COUNT) {
            throw new ManagerLoadException(
                    String.format("Количество строк в файле %s меньше предусмотренного: %d < %d.",
                            file.toPath(), lines.length, DATA_FILE_MIN_LINES_COUNT));
        }

        int lineNumber;
        for (lineNumber = 1; lineNumber < lines.length; lineNumber++) { // перебор строк начинается со второй строки
            if (!lines[lineNumber].isEmpty()) {
                taskManager.addTaskOfAnyType(taskManager.fromString(lines[lineNumber]));
            } else {
                break;
            }
        }

        int historyLineNumber = lineNumber + 1;

        if (historyLineNumber < lines.length) {
            List<Integer> historyTasksId = historyFromString(lines[historyLineNumber]);
            for (Integer id : historyTasksId) {
                taskManager.addTaskToHistoryByGettingById(id);
            }
        }

        return taskManager;
    }

    /**
     * Метод добавляет задачу в историю, запрашивая её через id
     */
    private void addTaskToHistoryByGettingById(int id) {
        if (this.getTask(id) != null) {
            return;
        }
        if (this.getSubtask(id) != null) {
            return;
        }
        this.getEpic(id);
    }

    // Методы, изменяющие задачи, переопределяются, чтобы при каждом изменении происходило автосохранение в файл.

    @Override
    public void removeAllTasks() throws ManagerSaveException {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() throws ManagerSaveException {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() throws ManagerSaveException {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public int addTaskOfAnyType(Task task) throws ManagerSaveException {
        int id = super.addTaskOfAnyType(task);
        save();
        return id;
    }

    @Override
    public boolean replaceTask(Task task) throws ManagerSaveException {
        boolean isSuccessfullyReplacing = super.replaceTask(task);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public boolean replaceEpic(Epic epic) throws ManagerSaveException {
        boolean isSuccessfullyReplacing = super.replaceEpic(epic);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public boolean replaceSubtask(Subtask subtask) throws ManagerSaveException {
        boolean isSuccessfullyReplacing = super.replaceSubtask(subtask);
        save();
        return isSuccessfullyReplacing;
    }

    @Override
    public void removeTaskOfAnyTypeById(int id) throws ManagerSaveException {
        super.removeTaskOfAnyTypeById(id);
        save();
    }

    // Методы просмотра задач также переопределяются, поскольку они вызывают изменение истории просмотров. Изменения
    // в истории просмотров, таким образом, тоже автосохраняются в файл.

    @Override
    public Task getTask(int id) throws ManagerSaveException {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) throws ManagerSaveException {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) throws ManagerSaveException {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }
}