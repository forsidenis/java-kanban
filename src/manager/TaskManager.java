package manager;

import task.Epic;
import task.Subtask;
import task.Task;
import java.util.List;

public interface TaskManager {
    // Методы для задач
    List<Task> getAllTasks();

    void deleteAllTasks();
    Task getTaskById(int id);
    void createTask(Task task);
    void updateTask(Task task);
    void deleteTask(int id);

    // Методы для эпиков
    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpicById(int id);
    void createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpic(int id);

    // Методы для подзадач
    List<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtaskById(int id);
    void createSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtask(int id);

    // Дополнительные методы
    List<Subtask> getSubtasksByEpicId(int epicId);
    List<Task> getHistory();
}