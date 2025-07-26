package manager;

import org.junit.jupiter.api.Test;
import task.Task;
import task.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
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
    void historySizeLimit() {
        HistoryManager manager = new InMemoryHistoryManager();

        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description");
            task.setId(i);
            manager.add(task);
        }

        List<Task> history = manager.getHistory();
        assertEquals(10, history.size(), "История должна содержать максимум 10 задач");
        assertEquals(6, history.get(0).getId(), "Первая задача в истории должна быть Task 6");
        assertEquals(15, history.get(9).getId(), "Последняя задача в истории должна быть Task 15");
    }

    @Test
    void allowDuplicates() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task = new Task("Test task", "Description");
        task.setId(1);

        manager.add(task);
        manager.add(task);

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "История должна содержать дубликаты");
        assertEquals(task, history.get(0), "Первая задача должна быть оригиналом");
        assertEquals(task, history.get(1), "Вторая задача должна быть дубликатом");
    }
}