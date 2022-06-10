import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Subtask;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HTTPTaskServerTest {
    private final String URL = "http://localhost:8080";
    private KVServer kvServer;
    private HttpTaskServer taskServer;
    private HttpClient client;
    private Gson gson;
    private HttpRequest request;
    private HttpResponse<String> response;
    private Task taskA;
    private Task taskB;
    private Epic epicA;
    private Epic epicB;
    private Subtask subtaskA;
    private Subtask subtaskB;
    private Subtask subtaskC;
    private final String TASK_ENDPOINT = "/tasks/task/";
    private final String EPIC_ENDPOINT = "/tasks/epic/";
    private final String SUBTASK_ENDPOINT = "/tasks/subtask/";

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
                LocalDateTime.of(2022, 6, 10, 12, 45), 15);
        taskB = new Task(2, "Task B", TaskStatus.IN_PROGRESS, "Task B description",
                LocalDateTime.of(2022, 6, 2, 15, 15), 20);
        epicA = new Epic(3, "Epic A", "Epic A description");
        epicB = new Epic(4, "Epic B", "Epic B description");
        subtaskA = new Subtask(5, "Subtask A", TaskStatus.NEW, "Subtask A description",
                LocalDateTime.of(2022, 6, 5, 10, 30), 30, epicA.getId());
        subtaskB = new Subtask(6, "Subtask B", TaskStatus.NEW, "Subtask B description",
                null, 10, epicA.getId());
        subtaskC = new Subtask(7, "Subtask B", TaskStatus.NEW, "Subtask B description",
                LocalDateTime.of(2022, 6, 1, 18, 0), 120, epicB.getId());
    }

    @AfterEach
    void stopServers() {
        kvServer.stop();
        taskServer.stop();
    }

    @Test
    @DisplayName("Тест на работу всех эндпойнтов, относящихся к задачам")
    void apiTaskEndpointsTest() throws IOException, InterruptedException {

        // Добавление задачи
        addTaskByHttpRequest(TASK_ENDPOINT, gson.toJson(taskA));

        // Проверка возвращения задачи по id
        Task taskAReturned = gson.fromJson(getTaskJsonByHttpRequest(TASK_ENDPOINT, taskA.getId()), Task.class);
        assertEquals(taskA, taskAReturned, "Возвращённая задача не равна отправленной.");

        // Проверка возвращения задачи в списке задач
        taskAReturned = gson.fromJson(getJsonArrayByHttpRequest(TASK_ENDPOINT).get(0), Task.class);
        assertEquals(taskA, taskAReturned, "Возвращённая задача в списке задач не равна отправленной.");

        // Обновление статуса задачи
        taskA = new Task(1, "Task A", TaskStatus.DONE, "Task A description",
                LocalDateTime.of(2022, 6, 10, 12, 45), 15);
        addTaskByHttpRequest(TASK_ENDPOINT, gson.toJson(taskA));
        taskAReturned = gson.fromJson(getTaskJsonByHttpRequest(TASK_ENDPOINT, taskA.getId()), Task.class);
        assertEquals(TaskStatus.DONE, taskAReturned.getStatus(), "Задача не обновлена.");

        // Добавление второй задачи
        addTaskByHttpRequest(TASK_ENDPOINT, gson.toJson(taskB));

        // Проверка количества задач в списке
        assertEquals(2, getJsonArrayByHttpRequest(TASK_ENDPOINT).size(),
                "Количество возвращаемых задач не соответствует количеству отправленных.");

        // Проверка количества задач в списке после удаления задачи по id
        deleteTasksByHttpRequest("/tasks/task/", taskB.getId());
        assertEquals(1, getJsonArrayByHttpRequest(TASK_ENDPOINT).size(),
                "После удаления задачи возвращается неверное количество задач.");

        // Проверка количества задач в списке после удаления всех задач
        deleteTasksByHttpRequest("/tasks/task/", 0);
        assertEquals(0, getJsonArrayByHttpRequest(TASK_ENDPOINT).size(),
                "После удаления всех задач возвращается непустой список.");
    }

    @Test
    @DisplayName("Тест на работу всех эндпойнтов, относящихся к эпикам и подзадачам")
    void apiEpicAndSubtasksEndpointsTest() throws IOException, InterruptedException {

        // Добавление эпиков и подзадач
        addTaskByHttpRequest(EPIC_ENDPOINT, gson.toJson(epicA));
        addTaskByHttpRequest(EPIC_ENDPOINT, gson.toJson(epicB));

        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskA));
        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskB));
        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskC));

        // Проверка возвращения эпика по id (сравниваем эпики по именам, поскольку в объект на сервере уже отличается
        // - к нему присоединились подзадачи)
        String nameOfReturnedEpic = gson.fromJson(getTaskJsonByHttpRequest(EPIC_ENDPOINT,
                epicA.getId()), Epic.class).getName();
        assertEquals(epicA.getName(), nameOfReturnedEpic,
                "Имя возвращённого эпика на совпадает с отправленным.");

        // Обновление имени эпика
        epicA = new Epic(3, "New Epic A", "Epic A description");
        addTaskByHttpRequest(EPIC_ENDPOINT, gson.toJson(epicA));
        Epic epicAReturned = gson.fromJson(getTaskJsonByHttpRequest(EPIC_ENDPOINT, epicA.getId()), Epic.class);
        assertEquals("New Epic A", epicAReturned.getName(), "Эпик не обновлён.");

        // Проверка возвращения подзадачи по id
        Subtask subtaskAReturned = gson.fromJson(getTaskJsonByHttpRequest(SUBTASK_ENDPOINT,
                subtaskA.getId()), Subtask.class);
        assertEquals(subtaskA, subtaskAReturned, "Возвращённая подзадача не равна отправленной.");

        // Обновление статуса подзадачи
        subtaskA = new Subtask(5, "Subtask A", TaskStatus.DONE, "Subtask A description",
                LocalDateTime.of(2022, 6, 5, 10, 30), 30, epicA.getId());
        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskA));
        subtaskAReturned = gson.fromJson(getTaskJsonByHttpRequest(SUBTASK_ENDPOINT, subtaskA.getId()), Subtask.class);
        assertEquals(TaskStatus.DONE, subtaskAReturned.getStatus(), "Подзадача не обновлена.");

        // Проверка количества эпиков в списке эпиков
        assertEquals(2, getJsonArrayByHttpRequest(EPIC_ENDPOINT).size(),
                "Количество возвращаемых эпиков не соответствует количеству отправленных.");

        // Проверка количества подзадач в списке подзадач
        assertEquals(3, getJsonArrayByHttpRequest(SUBTASK_ENDPOINT).size(),
                "Количество возвращаемых подзадач не соответствует количеству отправленных.");

        // Проверка количества подзадач внутри 1-го эпика
        assertEquals(2, getJsonArrayByHttpRequest(SUBTASK_ENDPOINT + "epic/?id="
                        + epicA.getId()).size(),
                "Количество возвращаемых подзадач в эпике не соответствует количеству отправленных.");

        // Проверка количества подзадач внутри 2-го эпика
        assertEquals(1, getJsonArrayByHttpRequest(SUBTASK_ENDPOINT + "epic/?id="
                        + epicB.getId()).size(),
                "Количество возвращаемых подзадач в эпике не соответствует количеству отправленных.");

        // Проверка количества эпиков и подзадач после удаления по id эпика, включающего одну подзадачу
        deleteTasksByHttpRequest(EPIC_ENDPOINT, epicB.getId());
        assertEquals(1, getJsonArrayByHttpRequest(EPIC_ENDPOINT).size(),
                "После удаления эпика возвращается неверное количество эпиков.");
        assertEquals(2, getJsonArrayByHttpRequest(SUBTASK_ENDPOINT).size(),
                "После удаления эпика возвращается неверное количество подзадач.");

        // Проверка количества подзадач после удаления по id подзадачи
        deleteTasksByHttpRequest(SUBTASK_ENDPOINT, subtaskA.getId());
        assertEquals(1, getJsonArrayByHttpRequest(SUBTASK_ENDPOINT).size(),
                "После удаления подзадачи возвращается неверное количество подзадач.");

        // Проверка количества подзадач после удаления всех подзадач
        deleteTasksByHttpRequest(SUBTASK_ENDPOINT, 0);
        assertEquals(0, getJsonArrayByHttpRequest(SUBTASK_ENDPOINT).size(),
                "После удаления всех задач возвращается непустой список.");

        // Проверка количества эпиков после удаления всех эпиков
        deleteTasksByHttpRequest(EPIC_ENDPOINT, 0);
        assertEquals(0, getJsonArrayByHttpRequest(EPIC_ENDPOINT).size(),
                "После удаления всех эпиков возвращается непустой список.");
    }

    @Test
    @DisplayName("Тест на работу эндпойнта /tasks/history/")
    void apiHistoryEndpointsTest() throws IOException, InterruptedException {

        // Добавление задач, эпика и подзадач
        addTaskByHttpRequest(TASK_ENDPOINT, gson.toJson(taskA));
        addTaskByHttpRequest(EPIC_ENDPOINT, gson.toJson(epicA));
        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskA));
        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskB));

        // Имитация просмотра задачи, эпика и подзадачи
        getTaskJsonByHttpRequest(TASK_ENDPOINT, taskA.getId());
        getTaskJsonByHttpRequest(EPIC_ENDPOINT, epicA.getId());
        getTaskJsonByHttpRequest(SUBTASK_ENDPOINT, subtaskA.getId());

        // Проверка количества элементов в истории
        String HISTORY_ENDPOINT = "/tasks/history/";
        assertEquals(3, getJsonArrayByHttpRequest(HISTORY_ENDPOINT).size(),
                "Размер списка истории не соответствует количеству просмотренных задач.");
    }

    @Test
    @DisplayName("Тест на работу эндпойнта /tasks/")
    void apiPriorityEndpointsTest() throws IOException, InterruptedException {

        // Добавление задач, эпика и подзадач
        addTaskByHttpRequest(TASK_ENDPOINT, gson.toJson(taskA));
        addTaskByHttpRequest(TASK_ENDPOINT, gson.toJson(taskB));
        addTaskByHttpRequest(EPIC_ENDPOINT, gson.toJson(epicA));
        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskA));
        addTaskByHttpRequest(SUBTASK_ENDPOINT, gson.toJson(subtaskB));

        List<Task> tasks = new ArrayList<>();
        String PRIORITY_ENDPOINT = "/tasks/";
        getJsonArrayByHttpRequest(PRIORITY_ENDPOINT).forEach(x -> tasks.add(gson.fromJson(x, Task.class)));

        // Проверка количества элементов в истории
        assertEquals(4, tasks.size(),
                "Размер списка приоритетов не соответствует количеству задач и подзадач.");

        String message = "Неверный порядок элементов в списке приоритетов";
        assertEquals(2, tasks.get(0).getId(), message);
        assertEquals(5, tasks.get(1).getId(), message);
        assertEquals(1, tasks.get(2).getId(), message);
        assertEquals(6, tasks.get(3).getId(), message);
    }

    @Test
    @DisplayName("Тест на работу всех эндпойнтов при передаче некорректных данных")
    void apiIncorrectDataTest() throws IOException, InterruptedException {
        String message = "При ошибке в запросе код ответа не указывает на ошибку";

        assertEquals(404, getResponseCodeByHttpRequest("/somewhere/", 0), message);
        assertEquals(404, getResponseCodeByHttpRequest("/tasks/somewhere/?task=1", 0),
                "message");
        assertEquals(404, getResponseCodeByHttpRequest(TASK_ENDPOINT, 100500), message);
        assertEquals(404, getResponseCodeByHttpRequest(EPIC_ENDPOINT, 100500), message);
        assertEquals(404, getResponseCodeByHttpRequest(SUBTASK_ENDPOINT, 100500), message);
        assertEquals(404, getResponseCodeByHttpRequest(SUBTASK_ENDPOINT + "epic/", 100500),
                message);
        assertEquals(404, deleteTasksByHttpRequest("/somewhere/", 0), message);
        assertEquals(404, deleteTasksByHttpRequest("/tasks/somewhere/?task=1", 0),
                "message");
        assertEquals(404, deleteTasksByHttpRequest(TASK_ENDPOINT, 100500), message);
        assertEquals(404, deleteTasksByHttpRequest(EPIC_ENDPOINT, 100500), message);
        assertEquals(404, deleteTasksByHttpRequest(SUBTASK_ENDPOINT, 100500), message);

        // Передаём пустые данные в запросе на добавление
        assertEquals(400, addTaskByHttpRequest(TASK_ENDPOINT, ""), message);
        // Передаём некорректный jSON
        assertEquals(400, addTaskByHttpRequest(TASK_ENDPOINT, "{&*(^)}"), message);
        // Передаём задачу с некорректным id
        taskA = new Task(-18, "Task A", TaskStatus.NEW, "Task A description",
                LocalDateTime.of(2022, 6, 10, 12, 45), 15);
        assertEquals(400, addTaskByHttpRequest(TASK_ENDPOINT, gson.toJson(taskA)), message);
    }

    private int addTaskByHttpRequest(String endpoint, String json) throws IOException, InterruptedException {
        URI uri = URI.create(URL + endpoint);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return (response != null) ? response.statusCode() : 0;
    }

    private String getTaskJsonByHttpRequest(String endpoint, int id) throws IOException, InterruptedException {
        URI uri = URI.create(URL + endpoint + "?id=" + id);
        request = HttpRequest.newBuilder().uri(uri).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private int getResponseCodeByHttpRequest(String endpoint, int id) throws IOException, InterruptedException {
        URI uri = URI.create((URL + endpoint + ((id != 0) ? ("?id=" + id) : "")));
        request = HttpRequest.newBuilder().uri(uri).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return (response != null) ? response.statusCode() : 0;
    }

    private JsonArray getJsonArrayByHttpRequest(String endpoint) throws IOException, InterruptedException {
        URI uri = URI.create(URL + endpoint);
        request = HttpRequest.newBuilder().uri(uri).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    private int deleteTasksByHttpRequest(String endpoint, int id) throws IOException, InterruptedException {
        URI uri = URI.create((URL + endpoint + ((id != 0) ? ("?id=" + id) : "")));
        request = HttpRequest.newBuilder().uri(uri).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return (response != null) ? response.statusCode() : 0;
    }


}