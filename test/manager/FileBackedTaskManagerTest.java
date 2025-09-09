package manager;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    private Path tempDir;
    private File testFile;

    @BeforeEach
    public void setUp() {
        testFile = tempDir.resolve("test.csv").toFile();
        manager = new FileBackedTaskManager(testFile);
    }

    @Test
    @Override
    protected void shouldCalculateEpicStatus() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setStatus(Status.DONE);
        manager.createSubtask(subtask);

        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    public void shouldSaveAndLoadFromFile() throws IOException {
        // Создаем задачи
        Task task = new Task("Task", "Description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        manager.createTask(task);

        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setStartTime(LocalDateTime.now().plusHours(2));
        subtask.setDuration(Duration.ofHours(1));
        manager.createSubtask(subtask);

        // Просматриваем для истории
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем данные
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());
        assertEquals(3, loadedManager.getHistory().size());

        // Проверяем временные параметры
        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());
    }

    @Test
    public void shouldHandleEmptyFile() throws IOException {
        // Создаем пустой файл перед загрузкой
        testFile.createNewFile();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
    }
}