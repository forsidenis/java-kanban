package manager;

import task.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;

        if (history.size() >= MAX_HISTORY) {
            history.remove(0);
        }
        history.add(task);
    }

    @Override
    public void remove(int id) {
        Iterator<Task> iterator = history.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getId() == id) {
                iterator.remove();
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
