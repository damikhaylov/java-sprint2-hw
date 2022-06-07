package ru.yandex.practicum.tasktracker.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.tasktracker.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpTaskServer {
    private static final int PORT = 8080;
    static TaskManager taskManager = Managers.getDefault();


    public static void main(String[] args) {
        try {
            HttpServer httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TasksHandler());
            httpServer.start();
            System.out.println("HTTP-сервер запущен на " + PORT + " порту.");
            //httpServer.stop(1); // остановка сервера
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static OptionalInt getIdFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return OptionalInt.empty();
        }
        Pattern pattern = Pattern.compile("id=(\\d+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            return OptionalInt.of(id);
        } else {
            return OptionalInt.empty();
        }
    }

    static class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            GsonBuilder gsonBuilder = new GsonBuilder()
                    .serializeNulls()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
            Gson gson = gsonBuilder.create();

            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();
            OptionalInt id = getIdFromQuery(httpExchange.getRequestURI().getQuery());
            InputStream inputStream = httpExchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            List<String> contentType = httpExchange.getRequestHeaders().get("Content-type");
            boolean isJsonContent = ((contentType != null) && (contentType.contains("application/json")));

            String response = "";

            switch (method + " " + path) { // Сращиваем метод и путь в единую сигнатуру для упрощения условий
                case "GET /tasks/task/":
                    if (id.isPresent()) {
                        response = gson.toJson(taskManager.getTask(id.getAsInt()));
                    } else {
                        response = gson.toJson(taskManager.getTasks());
                    }
                    break;
                case "POST /tasks/task/":
                    if (!body.isBlank() && isJsonContent) {
                        System.out.println("Добавление задачи");
                        Task task = gson.fromJson(body, Task.class);
                    }
                    break;
                case "DELETE /tasks/task/":
                    if (id.isPresent()) {
                        taskManager.removeTaskOfAnyTypeById(id.getAsInt());
                    } else {
                        taskManager.removeAllTasks();
                    }
                    break;
                case "GET /tasks/subtask/":
                    if (id.isPresent()) {
                        response = gson.toJson(taskManager.getSubtask(id.getAsInt()));
                    } else {
                        response = gson.toJson(taskManager.getSubtasks());
                    }
                    break;
                case "DELETE /tasks/subtask/":
                    if (id.isPresent()) {
                        taskManager.removeTaskOfAnyTypeById(id.getAsInt());
                    } else {
                        taskManager.removeAllSubtasks();
                    }
                    break;
                case "GET /tasks/epic/":
                    if (id.isPresent()) {
                        response = gson.toJson(taskManager.getEpic(id.getAsInt()));
                    } else {
                        response = gson.toJson(taskManager.getEpics());
                    }
                    break;
                case "GET /tasks/subtask/epic/":
                    if (id.isPresent()) {
                        response = gson.toJson(taskManager.getEpicSubtasks(id.getAsInt()));
                    }
                    break;
                case "DELETE /tasks/epic/":
                    if (id.isPresent()) {
                        taskManager.removeTaskOfAnyTypeById(id.getAsInt());
                    } else {
                        taskManager.removeAllEpics();
                    }
                    break;
                case "GET /tasks/history/":
                    response = gson.toJson(taskManager.getHistory());
                    ;
                    break;
                case "GET /tasks/":
                    response = gson.toJson(taskManager.getPrioritizedTasks());
                    break;
                default:
                    response = "Действие не определено.";
            }

            httpExchange.sendResponseHeaders(200, 0);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}

class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (localDateTime != null) {
            jsonWriter.value(localDateTime.format(formatter));
        } else {
            jsonWriter.value("null");
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return (!value.equals("null")) ? LocalDateTime.parse(value, formatter) : null;
    }
}
