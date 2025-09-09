package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    @Override
    protected void shouldCalculateEpicStatus() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        // Все подзадачи NEW
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.NEW);
        manager.createSubtask(subtask1);
        assertEquals(Status.NEW, epic.getStatus());

        // Все подзадачи DONE
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        assertEquals(Status.DONE, epic.getStatus());

        // Подзадачи NEW и DONE
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.NEW);
        manager.createSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        // Подзадачи IN_PROGRESS
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void shouldRemoveTaskFromHistory() {
        Task task = new Task("Task", "Description");
        manager.createTask(task);
        manager.getTaskById(task.getId());

        assertFalse(manager.getHistory().isEmpty());
        manager.deleteTask(task.getId());
        assertTrue(manager.getHistory().isEmpty());
    }
}