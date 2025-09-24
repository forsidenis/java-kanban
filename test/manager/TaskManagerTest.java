package manager;

import exceptions.NotFoundException;
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

        // Создаем эпик с подзадачами для проверки статуса
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setStartTime(now.plusHours(3));
        subtask.setDuration(Duration.ofHours(1));
        subtask.setStatus(Status.DONE); // Явно устанавливаем статус
        manager.createSubtask(subtask);

        manager.createTask(task1);
        assertThrows(ManagerValidationException.class, () -> manager.createTask(task2));

        // ДОБАВЛЯЕМ ПРОВЕРКУ СТАТУСА EPIC
        assertEquals(Status.DONE, epic.getStatus(),
                "Статус эпика должен быть DONE, так как подзадача имеет статус DONE");
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

        // Создаем эпик с подзадачами для проверки статуса
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setStartTime(now.plusHours(4));
        subtask.setDuration(Duration.ofHours(1));
        subtask.setStatus(Status.NEW); // Явно устанавливаем статус
        manager.createSubtask(subtask);

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(3, prioritized.size());
        assertEquals(task2.getId(), prioritized.get(0).getId());
        assertEquals(task1.getId(), prioritized.get(1).getId());

        // ДОБАВЛЯЕМ ПРОВЕРКУ СТАТУСА EPIC
        assertEquals(Status.NEW, epic.getStatus(),
                "Статус эпика должен быть NEW, так как подзадача имеет статус NEW");
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
        subtask1.setStatus(Status.IN_PROGRESS); // Явно устанавливаем статус

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStartTime(now.plusHours(2));
        subtask2.setDuration(duration);
        subtask2.setStatus(Status.IN_PROGRESS); // Явно устанавливаем статус

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Проверяем время
        assertEquals(now, epic.getStartTime());
        assertEquals(now.plusHours(3), epic.getEndTime());
        assertEquals(Duration.ofHours(2), epic.getDuration());

        // ДОБАВЛЯЕМ ПРОВЕРКУ СТАТУСА
        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика должен быть IN_PROGRESS, так как все подзадачи имеют статус IN_PROGRESS");
    }

    @Test
    protected void shouldHandleTasksWithoutTime() {
        Task task = new Task("Task", "Description");
        manager.createTask(task);

        // Создаем эпик с подзадачами без времени для проверки статуса
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setStatus(Status.NEW); // Не устанавливаем время, только статус
        manager.createSubtask(subtask);

        assertNull(task.getStartTime());
        assertNull(task.getEndTime());
        assertEquals(0, manager.getPrioritizedTasks().size());

        // ДОБАВЛЯЕМ ПРОВЕРКУ СТАТУСА EPIC
        assertEquals(Status.NEW, epic.getStatus(),
                "Статус эпика должен быть NEW, так как подзадача имеет статус NEW");
    }

    @Test
    protected void shouldHandleEmptyCollections() {
        // b. С пустым списком задач
        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
        assertTrue(manager.getPrioritizedTasks().isEmpty());
    }

    @Test
    protected void shouldHandleInvalidIds() {
        assertThrows(NotFoundException.class, () -> manager.getTaskById(-1));
        assertThrows(NotFoundException.class, () -> manager.getEpicById(-1));
        assertThrows(NotFoundException.class, () -> manager.getSubtaskById(-1));
    }

    @Test
    protected void shouldHandleDeletionOfNonExistentTasks() {
        assertDoesNotThrow(() -> manager.deleteTask(-1));
        assertDoesNotThrow(() -> manager.deleteEpic(-1));
        assertDoesNotThrow(() -> manager.deleteSubtask(-1));
    }

    @Test
    protected void shouldHandleUpdatesOfNonExistentTasks() {
        Task task = new Task("Test", "Description");
        task.setId(-1);
        assertDoesNotThrow(() -> manager.updateTask(task));

        Epic epic = new Epic("Test", "Description");
        epic.setId(-1);
        assertDoesNotThrow(() -> manager.updateEpic(epic));
    }

    @Test
    protected void shouldReturnEmptySubtasksForInvalidEpic() {
        // c. С неверным идентификатором эпика при получении подзадач
        List<Subtask> subtasks = manager.getSubtasksByEpicId(-1);
        assertNotNull(subtasks);
        assertTrue(subtasks.isEmpty());
    }


    @Test
    protected void shouldCalculateEpicStatusWithEmptySubtasks() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        // a. Пустой список подзадач
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    protected void shouldCalculateEpicStatusWithAllNewSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        // Все подзадачи со статусом NEW
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.NEW);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.NEW);
        manager.createSubtask(subtask2);

        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика со всеми NEW подзадачами должен быть NEW");
    }

    @Test
    protected void shouldCalculateEpicStatusWithAllDoneSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        // Все подзадачи со статусом DONE
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.DONE);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.DONE);
        manager.createSubtask(subtask2);

        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика со всеми DONE подзадачами должен быть DONE");
    }

    @Test
    protected void shouldCalculateEpicStatusWithNewAndDoneSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        // Подзадачи со статусами NEW и DONE
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.NEW);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.DONE);
        manager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика с NEW и DONE подзадачами должен быть IN_PROGRESS");
    }

    @Test
    protected void shouldCalculateEpicStatusWithInProgressSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        // Подзадачи со статусом IN_PROGRESS
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика со всеми IN_PROGRESS подзадачами должен быть IN_PROGRESS");
    }
}
