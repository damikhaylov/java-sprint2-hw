package ru.yandex.practicum.tasktracker.manager;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import ru.yandex.practicum.tasktracker.exeption.ManagerSaveException;
import ru.yandex.practicum.tasktracker.model.Epic;
import ru.yandex.practicum.tasktracker.model.Task;
import ru.yandex.practicum.tasktracker.server.KVTaskClient;

import java.util.HashMap;

public class HTTPTaskManager extends FileBackedTaskManager {

    private KVTaskClient client;
    private Gson gson;

    public HTTPTaskManager(String source, boolean isSourceForReadData) {
        super(source, isSourceForReadData);
    }

    @Override
    protected void init(String source, boolean isSourceForReadData) {
        client = new KVTaskClient(source);
        gson = Managers.getGson();
        if (isSourceForReadData) {
            load();
        }
    }

    @Override
    public void save() throws ManagerSaveException {
        client.put("tasks", gson.toJson(tasks));
        client.put("epics", gson.toJson(epics));
        client.put("history", gson.toJson(getHistory().stream().map(Task::getId).toArray()));
    }

    @Override
    public void load() {
        prioritizedTasks.clear();

        tasks.clear();
        tasks.putAll(gson.fromJson(client.load("tasks"), new TypeToken<HashMap<Integer, Task>>() {
        }.getType()));
        prioritizedTasks.addAll(tasks.values());

        epics.clear();
        epics.putAll(gson.fromJson(client.load("epics"), new TypeToken<HashMap<Integer, Epic>>() {
        }.getType()));

        subtasks.clear();
        epics.values().stream().map(Epic::getSubtasksMap).forEach(subtasks::putAll);
        prioritizedTasks.addAll(subtasks.values());

        JsonParser.parseString(client.load("history")).getAsJsonArray()
                .forEach(x -> addTaskToHistory(x.getAsInt()));
    }
}
