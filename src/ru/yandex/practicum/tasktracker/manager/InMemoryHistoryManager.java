package ru.yandex.practicum.tasktracker.manager;

import ru.yandex.practicum.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Node<Task> head;
    private Node<Task> tail;
    private final Map<Integer, Node<Task>> historyMap;

    public InMemoryHistoryManager() {
        historyMap = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            int id = task.getId();
            if (historyMap.containsKey(id)) {
                removeNode(historyMap.get(id));
            }
            linkLast(task);
            historyMap.put(id, tail);
        }
    }

    private void removeNode(Node<Task> node) {
        if (!node.equals(head)) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (!node.equals(tail)) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task element) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(oldTail, element, null);
        tail = newNode;
        if (oldTail == null)
            head = newNode;
        else
            oldTail.next = newNode;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> node = head;
        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }
        return tasks;
    }
}

