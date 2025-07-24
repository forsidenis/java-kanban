package manager;

import task.Task;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;

        if (history.size() >= MAX_HISTORY) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public void remove(int id) {
        history.removeIf(task -> task.getId() == id);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
