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

        // Если файл-аргумент конструктора существует, то будет выполнено считывание данных из него,
        // если нет, то менеджер будет создан пустым, а файл будет создан и заполнен при первом сохранении данных
        if (this.file.exists()) {
            loadFromFile(); // Поскольку метод должен вызываться из конструктора, он уже не может иметь вид
            // static FileBackedTasksManager loadFromFile(File file), как в техзадании
        }
    }

    public static void main(String[] args) {
        // для создания первого менеджера будем использовать Managers.getDefault(), поэтому будем работать в дальнейшем
        // с именем файла, которое используется по-умолчанию для создания менеджера
        String fileName = Managers.DEFAULT_BACKUP_FILE_NAME;

        // для теста удаляем файл данных, чтобы первый менеджер начал работать "с нуля"
        new File(fileName).delete();

        // Создание первого менеджера для добавления данных
        TaskManager taskManager = Managers.getDefault();

        // Использование методов специально созданного класса TestScenario для добавления тестовых данных
        TestScenario test = new TestScenario(taskManager);
        test.Add2Tasks2Epics3Subtasks(); // добавление двух задач, двух эпиков и трёх подзадач
        test.View2Tasks1Epic(); // имитация просмотра двух задач и эпика
        System.out.printf("%n>>>>> Тестовые данные были добавлены в менеджер и сохранены в файл %s%n%n", fileName);

        System.out.printf(">>>>> Загрузка тестовых данных в новый менеджер из файла %s%n%n", fileName);

        // Создание второго менеджера и считывание в него данных, сохранённых первым менеджером
        FileBackedTasksManager newTaskManager = new FileBackedTasksManager(new File(fileName));

        System.out.println("\n>>>>> Список задач (подгружен из нового менеджера):");
        System.out.println(newTaskManager.getCSVForAllTasks());
        System.out.println(">>>>> История просмотров (подгружена из нового менеджера):");
        System.out.println(toString(newTaskManager.historyManager));
    }

    /**
     * Метод сохраняет задачи и историю просмотров в файл
     */
    public void save() throws ManagerSaveException {
        String fileName = file.getName();
        StringBuilder stringBuilder = new StringBuilder(getCSVForAllTasks());
        stringBuilder.append("\n");
        stringBuilder.append(toString(this.historyManager));
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
            // Исключение будет обработано в методе loadFromFile
            throw new ManagerLoadException(String.format("Некорректное число полей данных задачи ( = %d) в строке:%n%s",
                    fields.length, value));
        }

        try {
            id = Integer.parseInt(fields[0]);   // соответствие с nextTaskId реализовано в методе loadFromFile
            // после добавления задачи в Map
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
            Epic epic = this.epics.getOrDefault(epicId, null);
            if (epic == null) {
                // Исключение будет обработано в методе loadFromFile
                throw new ManagerLoadException(
                        String.format("Подзадача ссылается на несуществующий эпик в строке:%n%s", value));
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
                // Исключение будет обработано в методе loadFromFile
                throw new ManagerLoadException(
                        String.format("Идентификатор задачи в истории просмотров - не целое число: %s", id), exception);
            }
        }
        return historyTasksId;
    }

    /**
     * Метод создаёт и возвращает менеджер, заполняя его данными из файла формата csv
     */
    private void loadFromFile() {
        String csv;

        try {
            csv = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.out.printf("Ошибка чтения файла данных %s%n", file.toPath());
            return;
        }

        String[] lines = csv.split("\\n");

        if (lines.length < DATA_FILE_MIN_LINES_COUNT) {
            System.out.printf("Количество строк в файле %s меньше предусмотренного: %d < %d.",
                    file.toPath(), lines.length, DATA_FILE_MIN_LINES_COUNT);
            return;
        }

        int lineNumber;
        for (lineNumber = 1; lineNumber < lines.length; lineNumber++) { // перебор строк начинается со второй строки
            if (!lines[lineNumber].isEmpty()) {
                Task task;

                try { // строка парсится в задачу с отловом возможных ошибок
                    task = this.fromString(lines[lineNumber]);
                } catch (ManagerLoadException exception) {
                    System.out.printf("Из-за ошибок не удалось загрузить данные из файла %s%n", file.toPath());
                    System.out.println(exception.getMessage());

                    // Поскольку при ошибке считывания какой-либо задачи может быть нарушена целостность всех данных
                    // из-за связей эпик-подзадача, а также целостность данных по истории просмотров, то частичное
                    // считывание задач не реализуем, а просто очищаем менеджер от всех считанных данных.
                    this.tasks.clear();
                    this.epics.clear();
                    this.subtasks.clear();

                    return; // завершаем работу метода, менеджер останется пустым
                }

                if (task.getClass() == Epic.class) {
                    this.epics.put(task.getId(), (Epic) task);
                } else if (task.getClass() == Subtask.class) {
                    Subtask subtask = (Subtask) task;
                    Epic epic = subtask.getEpic();
                    this.subtasks.put(subtask.getId(), subtask);
                    epic.getSubtasksIdSet().add(subtask.getId());
                } else {
                    this.tasks.put(task.getId(), task);
                }

                // id задач при загрузке из файла в менеджер должно сохраняться, иначе нарушится консистентность ссылок
                // подзадач на эпики и списка истории просмотров. При этом необходимо синхронизовать нумерацию задач,
                // считанных из файла, с задачами, которые будут добавлены через менеджер позднее. Для этого поле
                // nextTaskId будем назначать на 1 больше максимального id среди задач, считанных из файла.
                if (task.getId() >= this.nextTaskId) {
                    this.nextTaskId = task.getId() + 1;
                }
            } else {
                break;
            }
        }

        int historyLineNumber = lineNumber + 1;

        if (historyLineNumber < lines.length) {
            List<Integer> historyTasksId;

            try { // строка истории парсится в список с отловом возможных ошибок
                historyTasksId = historyFromString(lines[historyLineNumber]);
            } catch (ManagerLoadException exception) {
                System.out.printf("Возникли ошибки при чтении файла %s%n", file.toPath());
                System.out.println(exception.getMessage());
                System.out.println("История просмотров не будет считана.");

                return; // завершаем работу метода, менеджер будет заполнен задачами, но с пустой историей
            }

            for (Integer id : historyTasksId) {
                this.addTaskToHistory(id);
            }
        }
    }

    /**
     * Метод добавляет задачу в историю
     */
    private void addTaskToHistory(int id) {
        if (this.epics.containsKey(id)) {
            this.historyManager.add(this.epics.get(id));
        } else if (this.subtasks.containsKey(id)) {
            this.historyManager.add(this.subtasks.get(id));
        } else if (this.tasks.containsKey(id)) {
            this.historyManager.add(this.tasks.get(id));
        }
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