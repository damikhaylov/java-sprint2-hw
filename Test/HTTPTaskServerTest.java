import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.tasktracker.model.Task;
import ru.yandex.practicum.tasktracker.model.TaskStatus;
import ru.yandex.practicum.tasktracker.server.HttpTaskServer;
import ru.yandex.practicum.tasktracker.server.KVServer;
import ru.yandex.practicum.tasktracker.server.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HTTPTaskServerTest {
    private final String URL = "http://localhost:8080";
    private KVServer kvServer;
    private HttpTaskServer taskServer;
    private HttpClient client;
    private Gson gson;
    String json;
    URI uri;
    HttpRequest.BodyPublisher body;
    HttpRequest request;
    HttpResponse<String> response;
    Task taskA;
    Task taskAReturned;
    Task taskB;

    @BeforeEach
    void startServers() {
        try {
            kvServer = new KVServer();
            kvServer.start();
            taskServer = new HttpTaskServer();
            taskServer.start();
        } catch (IOException e) {
            System.out.println("Ошибка при запуске серверов.");
            e.printStackTrace();
        }
        client = HttpClient.newHttpClient();
        GsonBuilder gsonBuilder = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gson = gsonBuilder.create();
        taskA = new Task(1, "Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.of(2022, 6, 1, 12, 45), 15);
        taskB = new Task(2, "Task B", TaskStatus.IN_PROGRESS, "Task B description",
                LocalDateTime.of(2022, 6, 2, 15, 15), 20);
    }

    @AfterEach
    void stopServers() {
        kvServer.stop();
        taskServer.stop();
    }

    @Test
    @DisplayName("Тест на работу всех эндпойнтов, относящихся к задачам")
    void apiTaskEndpointsTest() throws IOException, InterruptedException {
        final String ENDPOINT = "/tasks/task/";

        // Добавление задачи
        addTaskByHttpRequest(ENDPOINT, gson.toJson(taskA));

        // Проверка возвращения задачи по id
        taskAReturned = gson.fromJson(getTaskJsonByHttpRequest(ENDPOINT, taskA.getId()), Task.class);
        assertEquals(taskA, taskAReturned, "Возвращённая задача не равна отправленной.");

        // Проверка возвращения задачи в списке задач
        taskAReturned = gson.fromJson(getJsonArrayByHttpRequest(ENDPOINT).get(0), Task.class);
        assertEquals(taskA, taskAReturned, "Возвращённая задача в списке задач не равна отправленной.");

        // Добавление второй задачи
        addTaskByHttpRequest(ENDPOINT, gson.toJson(taskB));

        // Проверка количества задач в списке
        assertEquals(2, getJsonArrayByHttpRequest(ENDPOINT).size(),
                "Количество возвращаемых задач не соответствует количеству отправленных.");

        // Проверка количества задач в списке после удаления задачи по id
        deleteTasksByHttpRequest("/tasks/task/", taskB.getId());
        assertEquals(1, getJsonArrayByHttpRequest(ENDPOINT).size(),
                "После удаления задачи возвращается неверное количество задач.");

        // Проверка количества задач в списке после удаления всех задач
        deleteTasksByHttpRequest("/tasks/task/", 0);
        assertEquals(0, getJsonArrayByHttpRequest(ENDPOINT).size(),
                "После удаления всех задач возвращается непустой список.");
    }

    void addEpicWith2SubtasksAndEpicWith1Subtask() {

    }

    private void addTaskByHttpRequest (String endpoint, String json) throws IOException, InterruptedException {
        URI uri = URI.create(URL + endpoint);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String getTaskJsonByHttpRequest(String endpoint, int id) throws IOException, InterruptedException {
        URI uri = URI.create(URL + endpoint + "?id=" + id);
        request = HttpRequest.newBuilder().uri(uri).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private JsonArray getJsonArrayByHttpRequest (String endpoint) throws IOException, InterruptedException {
        URI uri = URI.create(URL + endpoint);
        request = HttpRequest.newBuilder().uri(uri).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    private void deleteTasksByHttpRequest (String endpoint, int id) throws IOException, InterruptedException {
        URI uri = URI.create((URL + endpoint + ((id != 0) ? ("?id=" + id) : "")));
        request = HttpRequest.newBuilder().uri(uri).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }


}