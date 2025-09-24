package manager;

import exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import java.util.List;

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

    @Test
    public void shouldHandleEmptyTaskList() {
        // b. С пустым списком задач
        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    public void shouldHandleInvalidTaskId() {
        assertThrows(NotFoundException.class, () -> manager.getTaskById(-1),
                "Несуществующий ID задачи должен бросать исключение");
        assertThrows(NotFoundException.class, () -> manager.getEpicById(-1),
                "Несуществующий ID эпика должен бросать исключение");
        assertThrows(NotFoundException.class, () -> manager.getSubtaskById(-1),
                "Несуществующий ID подзадачи должен бросать исключение");
    }

    @Test
    public void shouldHandleTaskDeletionWithInvalidId() {
        assertDoesNotThrow(() -> manager.deleteTask(-1),
                "Удаление несуществующей задачи не должно вызывать исключение");
        assertDoesNotThrow(() -> manager.deleteEpic(-1),
                "Удаление несуществующего эпика не должно вызывать исключение");
        assertDoesNotThrow(() -> manager.deleteSubtask(-1),
                "Удаление несуществующей подзадачи не должно вызывать исключение");
    }

    @Test
    public void shouldHandleTaskUpdateWithInvalidId() {
        Task invalidTask = new Task("Invalid", "Description");
        invalidTask.setId(-1);

        assertDoesNotThrow(() -> manager.updateTask(invalidTask),
                "Обновление несуществующей задачи не должно вызывать исключение");

        Epic invalidEpic = new Epic("Invalid", "Description");
        invalidEpic.setId(-1);

        assertDoesNotThrow(() -> manager.updateEpic(invalidEpic),
                "Обновление несуществующего эпика не должно вызывать исключение");
    }

    @Test
    public void shouldReturnEmptyListForInvalidEpicId() {
        List<Subtask> subtasks = manager.getSubtasksByEpicId(-1);
        assertTrue(subtasks.isEmpty(),
                "Получение подзадач для несуществующего эпика должно возвращать пустой список");
    }

}