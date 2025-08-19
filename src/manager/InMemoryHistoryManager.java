package manager;

import task.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private final Node head;
    private final Node tail;

    public InMemoryHistoryManager() {
        head = new Node(null, null, null);
        tail = new Node(null, null, null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void add(Task task) {
        if (task == null) return;
        remove(task.getId());
        linkLast(task);
    }

    private void linkLast(Task task) {
        Node last = tail.prev;
        Node newNode = new Node(task, last, tail);
        last.next = newNode;
        tail.prev = newNode;
        historyMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        historyMap.remove(node.task.getId());
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        if (node != null) {
            removeNode(node);
        }
    }


    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head.next;
        while (current != tail) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return (ArrayList<Task>) getTasks();
    }
}
