import ru.yandex.practicum.tasktracker.manager.KVServer;
import ru.yandex.practicum.tasktracker.manager.KVTaskClient;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new KVServer().start();

        KVTaskClient client = new KVTaskClient("http://localhost:8078");
        client.put("tasks", "{\"field\": \"text\"}");
        System.out.println(client.load("tasks"));
        client.put("tasks", "{\"field\": \"text2\"}");
        System.out.println(client.load("tasks"));
    }
}