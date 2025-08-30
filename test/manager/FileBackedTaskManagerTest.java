package manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Сохраняем пустое состояние
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
    }

    @Test
    void testSaveAndLoadWithTasks() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаем задачи
        Task task = new Task("Test Task", "Description");
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        manager.createSubtask(subtask);

        // Просматриваем задачи для истории
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем задачи
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        // Проверяем историю
        assertEquals(3, loadedManager.getHistory().size());

        // Проверяем связи
        List<Subtask> epicSubtasks = loadedManager.getSubtasksByEpicId(epic.getId());
        assertEquals(1, epicSubtasks.size());
        assertEquals(subtask.getId(), epicSubtasks.get(0).getId());
    }

    @Test
    void testEpicStatusCalculationAfterLoad() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаем эпик и подзадачи
        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Меняем статусы подзадач
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем статус эпика
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, loadedEpic.getStatus());
    }

    @Test
    void testHistoryPreservation() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаем задачи
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        manager.createTask(task1);
        manager.createTask(task2);

        // Просматриваем задачи
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId()); // Повторный просмотр

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем историю (должна содержать 2 уникальные задачи в правильном порядке)
        List<Task> history = loadedManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task1.getId(), history.get(1).getId());
    }
}