package manager;

import org.junit.jupiter.api.Test;
import task.Task;
import task.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    @Test
    void addAndGetHistory() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task = new Task("Test task", "Description");
        task.setId(1);
        task.setStatus(Status.IN_PROGRESS);

        manager.add(task);
        List<Task> history = manager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task, history.getFirst(), "Задачи в истории должны совпадать");
    }

    @Test
    public void removeDuplicates() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task = new Task("Test task", "Description");
        task.setId(1);

        manager.add(task);
        manager.add(task); // Дубликат

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только уникальные задачи");
    }

    @Test
    public void historyOrder() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task3", "Description");
        task3.setId(3);

        manager.add(task1);
        manager.add(task2);
        manager.add(task3);
        manager.add(task1); // Повторное добавление

        List<Task> history = manager.getHistory();
        assertEquals(List.of(task2, task3, task1), history,
                "Порядок задач должен соответствовать последним просмотрам");
    }

    @Test
    public void removeFromHistory() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task2", "Description");
        task2.setId(2);

        manager.add(task1);
        manager.add(task2);
        manager.remove(1);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    public void noDuplicatesInHistory() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task = new Task("Test task", "Description");
        task.setId(1);

        manager.add(task);
        manager.add(task); // Добавляем второй раз

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов");
    }
}