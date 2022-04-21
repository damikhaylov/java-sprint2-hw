package ru.yandex.practicum.tasktracker.manager;

import java.util.Objects;

public class Node<T> {
    public T data;
    public Node<T> next;
    public Node<T> prev;

    public Node(Node<T> prev, T data, Node<T> next) {
        this.data = data;
        this.next = next;
        this.prev = prev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node<?> node = (Node<?>) o;

        return Objects.equals(data, node.data)
                && Objects.equals(next, node.next)
                && Objects.equals(prev, node.prev);
    }

    @Override
    public int hashCode() {
        int hash = data != null ? data.hashCode() : 0;
        hash = 31 * hash + (next != null ? next.hashCode() : 0);
        hash = 31 * hash + (prev != null ? prev.hashCode() : 0);
        return hash;
    }
}
