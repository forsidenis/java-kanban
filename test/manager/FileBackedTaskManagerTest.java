package manager;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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
    public void testEpicStatusCalculationAfterLoad() {
        // Создаем эпик и подзадачи
        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(Status.NEW); // Явно устанавливаем статус
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(Status.NEW); // Явно устанавливаем статус
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Проверяем статус до изменения
        assertEquals(Status.NEW, epic.getStatus(),
                "Изначально статус эпика должен быть NEW");

        // Меняем статусы подзадач
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        // Проверяем статус после изменения
        assertEquals(Status.DONE, epic.getStatus(),
                "После изменения статусов подзадач статус эпика должен быть DONE");

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем статус эпика после загрузки
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, loadedEpic.getStatus(),
                "Статус эпика после загрузки из файла должен остаться DONE");
    }

    @Test
    public void shouldHandleEmptyTaskList() throws IOException {
        // b. С пустым списком задач
        testFile.createNewFile();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    public void shouldHandleInvalidTaskId() {
        // С неверным идентификатором задачи
        assertNull(manager.getTaskById(-1), "Несуществующий ID задачи должен возвращать null");
        assertNull(manager.getEpicById(-1), "Несуществующий ID эпика должен возвращать null");
        assertNull(manager.getSubtaskById(-1), "Несуществующий ID подзадачи должен возвращать null");
    }

    @Test
    public void shouldHandleTaskDeletionWithInvalidId() {
        // С неверным идентификатором задачи при удалении
        assertDoesNotThrow(() -> manager.deleteTask(-1),
                "Удаление несуществующей задачи не должно вызывать исключение");
        assertDoesNotThrow(() -> manager.deleteEpic(-1),
                "Удаление несуществующего эпика не должно вызывать исключение");
        assertDoesNotThrow(() -> manager.deleteSubtask(-1),
                "Удаление несуществующей подзадачи не должно вызывать исключение");
    }

    @Test
    public void shouldHandleTaskUpdateWithInvalidId() {
        // c. С неверным идентификатором задачи при обновлении
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
        //  С неверным идентификатором эпика при получении подзадач
        List<Subtask> subtasks = manager.getSubtasksByEpicId(-1);
        assertTrue(subtasks.isEmpty(),
                "Получение подзадач для несуществующего эпика должно возвращать пустой список");
    }

    @Test
    public void shouldHandleStandardBehavior() throws IOException {
        // Со стандартным поведением
        Task task = new Task("Test Task", "Description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        subtask.setStartTime(LocalDateTime.now().plusHours(2));
        subtask.setDuration(Duration.ofHours(1));
        manager.createSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем стандартное поведение после загрузки
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        Task retrievedTask = loadedManager.getTaskById(task.getId());
        assertNotNull(retrievedTask);
        assertEquals(task.getTitle(), retrievedTask.getTitle());
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
        subtask.setStatus(Status.IN_PROGRESS); // Явно устанавливаем статус
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

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus(),
                "Статус эпика должен соответствовать статусу его подзадач");
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