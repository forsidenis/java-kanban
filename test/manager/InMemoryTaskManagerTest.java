package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    public void createAndGetTask() {
        TaskManager manager = createManager();
        Task task = new Task("Test task", "Description");

        manager.createTask(task);
        Task savedTask = manager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    public void historyForDifferentTaskTypes() {
        TaskManager manager = createManager();

        Task task = new Task("Task", "Description");
        manager.createTask(task);

        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        manager.createSubtask(subtask);

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 задачи");
        assertEquals(task, history.get(0), "Первая задача должна быть Task");
        assertEquals(epic, history.get(1), "Вторая задача должна быть Epic");
        assertEquals(subtask, history.get(2), "Третья задача должна быть Subtask");
    }

    @Test
    public void updateEpicStatus() {
        TaskManager manager = createManager();
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Все подзадачи NEW -> эпик NEW
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW");

        // Одна подзадача IN_PROGRESS -> эпик IN_PROGRESS
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен быть IN_PROGRESS");

        // Все подзадачи DONE -> эпик DONE
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен быть DONE");
    }

    @Test
    public void deleteTaskRemovesFromHistory() {
        TaskManager manager = createManager();
        Task task = new Task("Task", "Description");
        manager.createTask(task);

        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size(), "Задача должна быть в истории");

        manager.deleteTask(task.getId());
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой после удаления задачи");
    }
}