package manager;

import exceptions.ManagerValidationException;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    @Test
    protected abstract void shouldCalculateEpicStatus();

    @Test
    protected void shouldCreateTask() {
        Task task = new Task("Test", "Description");
        manager.createTask(task);

        assertNotNull(task.getId());
        assertEquals(1, manager.getAllTasks().size());
    }

    @Test
    protected void shouldNotAllowTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusMinutes(30));
        task2.setDuration(Duration.ofHours(1));

        manager.createTask(task1);
        assertThrows(ManagerValidationException.class, () -> manager.createTask(task2));
    }

    @Test
    protected void shouldReturnPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now.plusHours(2));
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now);
        task2.setDuration(Duration.ofHours(1));

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId());
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }

    @Test
    protected void shouldCalculateEpicTime() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        LocalDateTime now = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(1);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStartTime(now);
        subtask1.setDuration(duration);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStartTime(now.plusHours(2));
        subtask2.setDuration(duration);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(now, epic.getStartTime());
        assertEquals(now.plusHours(3), epic.getEndTime());
        assertEquals(Duration.ofHours(2), epic.getDuration());
    }

    @Test
    protected void shouldHandleTasksWithoutTime() {
        Task task = new Task("Task", "Description");
        manager.createTask(task);

        assertNull(task.getStartTime());
        assertNull(task.getEndTime());
        assertEquals(0, manager.getPrioritizedTasks().size());
    }
}
