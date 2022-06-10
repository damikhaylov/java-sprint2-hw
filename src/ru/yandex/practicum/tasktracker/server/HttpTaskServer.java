package ru.yandex.practicum.tasktracker.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.tasktracker.manager.Managers;
import ru.yandex.practicum.tasktracker.manager.TaskManager;
import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
import ru.yandex.practicum.tasktracker.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager = Managers.getDefault();
    private final Gson gson;

    // TODO (Комментарий для код-ревью - удалить после спринта 7) - Для выполнении этого задания из-за особенностей
    //  библиотеки GSON пришлось внести изменения в модель (в классы эпика и подзадачи).
    //  1) Ранее свойства эпиков устанавливались через сеттеры, которые при этом отсутствовали в родительском классе
    //  Task. Это достигалось через скрытие полей родительского класса. Сериализация эпика через GSON при этом вызывала
    //  ошибку из-за дублирования полей.
    //  Сейчас свойства эпика устанавливаются через пересоздание объекта в конструкторе, как было рекомендовано
    //  техзаданием изначально.
    //  2) По результатам прошлого ревью было реализовано непосредственное хранение подзадач эпика в HashMap-свойстве
    //  эпика. (Раньше хранились только их id). Это позволило определять свойства эпика по свойствам подзадач «внутри»
    //  эпика. При этом каждая подзадача, в свою очередь, имела свойство, хранящее объект-родительский эпик.
    //  Сериализация эпиков и подзадач через GSON в таких условиях приводила к Stack Overflow.
    //  Сейчас подзадачи хранят только id родительского эпика.
    //  Эпики при этом сериализуются и десериализуются GSON вместе с подзадачами, что учтено при реализации
    //  HTTPTaskManager.

    public static void main(String[] args) {
        try {
            KVServer kvServer = new KVServer();
            kvServer.start();
            HttpTaskServer server = new HttpTaskServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpTaskServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this::mapRequests);
        GsonBuilder gsonBuilder = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gson = gsonBuilder.create();
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту.");
    }

    public void stop() {
        server.stop(0);
    }

    public void mapRequests(HttpExchange httpExchange) throws IOException {
        SimpleEntry<Integer, String> responseKV = new SimpleEntry<>(404, "Запрашиваемый объект не найден.");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        OptionalInt id = getIdFromQuery(httpExchange.getRequestURI().getQuery());
        InputStream inputStream = httpExchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        switch (method + " " + path) { // Сращиваем метод и путь в единую сигнатуру для упрощения условий
            case "GET /tasks/task/":
                if (id.isPresent()) {
                    Task task = taskManager.getTask(id.getAsInt());
                    responseKV = (task != null)
                            ? new SimpleEntry<>(200, gson.toJson(task))
                            : new SimpleEntry<>(404, "Запрашиваемый объект не найден.");
                } else {
                    responseKV = new SimpleEntry<>(200, gson.toJson(taskManager.getTasks()));
                }
                break;
            case "POST /tasks/task/":
                responseKV = postTask(body, Task.class);
                break;
            case "DELETE /tasks/task/":
                if (id.isPresent()) {
                    responseKV = (taskManager.removeTaskOfAnyTypeById(id.getAsInt()))
                            ? new SimpleEntry<>(201, "Задача удалена.")
                            : new SimpleEntry<>(404, "Запрашиваемый объект не найден.");
                } else {
                    taskManager.removeAllTasks();
                    responseKV = new SimpleEntry<>(201, "Все задачи удалены.");
                }
                break;
            case "GET /tasks/subtask/":
                if (id.isPresent()) {
                    Subtask subtask = taskManager.getSubtask(id.getAsInt());
                    responseKV = (subtask != null) ? new SimpleEntry<>(200, gson.toJson(subtask))
                            : new SimpleEntry<>(404, "Запрашиваемый объект не найден.");
                } else {
                    responseKV = new SimpleEntry<>(200, gson.toJson(taskManager.getSubtasks()));
                }
                break;
            case "POST /tasks/subtask/":
                responseKV = postTask(body, Subtask.class);
                break;
            case "DELETE /tasks/subtask/":
                if (id.isPresent()) {
                    responseKV = (taskManager.removeTaskOfAnyTypeById(id.getAsInt()))
                            ? new SimpleEntry<>(201, "Подзадача удалена.")
                            : new SimpleEntry<>(404, "Запрашиваемый объект не найден.");
                } else {
                    taskManager.removeAllSubtasks();
                    responseKV = new SimpleEntry<>(201, "Все подзадачи удалены.");
                }
                break;
            case "GET /tasks/epic/":
                if (id.isPresent()) {
                    Epic epic = taskManager.getEpic(id.getAsInt());
                    responseKV = (epic != null)
                            ? new SimpleEntry<>(200, gson.toJson(epic))
                            : new SimpleEntry<>(404, "Запрашиваемый объект не найден.");
                } else {
                    responseKV = new SimpleEntry<>(200, gson.toJson(taskManager.getEpics()));
                }
                break;
            case "GET /tasks/subtask/epic/":
                if (id.isPresent()) {
                    List<Subtask> subtasks = taskManager.getEpicSubtasks(id.getAsInt());
                    responseKV = (subtasks != null)
                            ? new SimpleEntry<>(200, gson.toJson(subtasks))
                            : new SimpleEntry<>(404, "Запрашиваемые объекты не найдены.");
                }
                break;
            case "POST /tasks/epic/":
                responseKV = postTask(body, Epic.class);
                break;
            case "DELETE /tasks/epic/":
                if (id.isPresent()) {
                    responseKV = (taskManager.removeTaskOfAnyTypeById(id.getAsInt()))
                            ? new SimpleEntry<>(201, "Эпик удалён.")
                            : new SimpleEntry<>(404, "Запрашиваемый объект не найден.");
                } else {
                    taskManager.removeAllEpics();
                    responseKV = new SimpleEntry<>(201, "Все эпики удалены.");
                }
                break;
            case "GET /tasks/history/":
                responseKV = new SimpleEntry<>(200, gson.toJson(taskManager.getHistory()));
                break;
            case "GET /tasks/":
                responseKV = new SimpleEntry<>(200, gson.toJson(taskManager.getPrioritizedTasks()));
                break;
            default:
                responseKV = new SimpleEntry<>(404, "Запрашиваемое действие или объект не найдены.");
        }

        String contentType = (responseKV.getKey() == 200) ? "application/json" : "text/plain";
        httpExchange.getResponseHeaders().set("Content-Type", contentType);
        httpExchange.sendResponseHeaders(responseKV.getKey(), 0);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(responseKV.getValue().getBytes());
        }
    }

    private static OptionalInt getIdFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return OptionalInt.empty();
        }
        Pattern pattern = Pattern.compile("id=(-?\\d+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            return OptionalInt.of(id);
        } else {
            return OptionalInt.empty();
        }
    }

    private <T extends Task> SimpleEntry<Integer, String> postTask(String json, Class<T> taskClass) {
        T task;

        if (json.isBlank()) {
            return new SimpleEntry<>(400, "Данные не переданы.");
        }

        try {
            task = gson.fromJson(json, taskClass);
        } catch (JsonSyntaxException e) {
            return new SimpleEntry<>(400, "Ошибка десериализации.");
        }

        if (taskManager.addTaskOfAnyType(task) != 0) {
            return new SimpleEntry<>(201, "Задача, эпик или подзадача успешно добавлена.");
        }

        boolean isUpdating;
        if (taskClass == Epic.class) {
            isUpdating = taskManager.replaceEpic((Epic) task);
        } else if (taskClass == Subtask.class) {
            isUpdating = taskManager.replaceSubtask((Subtask) task);
        } else {
            isUpdating = taskManager.replaceTask(task);
        }
        if (isUpdating) {
            return new SimpleEntry<>(201, "Задача, эпик или подзадача успешно обновлена.");
        }
        return new SimpleEntry<>(400, "Ошибка при добавлении или обновлении задачи, эпика или подзадачи.");
    }
}