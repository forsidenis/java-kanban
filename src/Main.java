import manager.Managers;
import manager.TaskManager;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        // Создание задач
        Task task1 = new Task("Помыть посуду", "Срочная задача");
        Task task2 = new Task("Сделать ДЗ", "По программированию");
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпиков с подзадачами
        Epic epic1 = new Epic("Организовать праздник", "Семейное торжество");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Купить продукты", "", epic1.getId());
        Subtask subtask2 = new Subtask("Пригласить гостей", "", epic1.getId());
        Subtask subtask3 = new Subtask("Заказать торт", "", epic1.getId()); // Добавляем третью подзадачу
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);

        // Создаем эпик без подзадач (новое требование)
        Epic epic2 = new Epic("Купить квартиру", "Недвижимость");
        manager.createEpic(epic2);

        System.out.println("=== Начальное состояние ===");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
        System.out.println("История: " + manager.getHistory());

        // Запрашиваем задачи несколько раз в разном порядке (новые проверки)
        System.out.println("\n=== Запросы задач в разном порядке ===");

        System.out.println("Запрос 1: Эпик с подзадачами");
        manager.getEpicById(epic1.getId());
        System.out.println("История: " + manager.getHistory());
        checkForDuplicates(manager);

        System.out.println("Запрос 2: Подзадача 1");
        manager.getSubtaskById(subtask1.getId());
        System.out.println("История: " + manager.getHistory());
        checkForDuplicates(manager);

        System.out.println("Запрос 3: Эпик без подзадач");
        manager.getEpicById(epic2.getId());
        System.out.println("История: " + manager.getHistory());
        checkForDuplicates(manager);

        System.out.println("Запрос 4: Задача 1");
        manager.getTaskById(task1.getId());
        System.out.println("История: " + manager.getHistory());
        checkForDuplicates(manager);

        System.out.println("Запрос 5: Эпик с подзадачами (повторно)");
        manager.getEpicById(epic1.getId());
        System.out.println("История: " + manager.getHistory());
        checkForDuplicates(manager);

        System.out.println("Запрос 6: Подзадача 2");
        manager.getSubtaskById(subtask2.getId());
        System.out.println("История: " + manager.getHistory());
        checkForDuplicates(manager);

        System.out.println("Запрос 7: Подзадача 1 (повторно)");
        manager.getSubtaskById(subtask1.getId());
        System.out.println("История: " + manager.getHistory());
        checkForDuplicates(manager);

        // Просматриваем задачи (оригинальные вызовы)
        System.out.println("\n--- Оригинальные просмотры ---");
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());

        // Вывод всех задач (оригинальные сообщения)
        System.out.println("--- Все задачи ---");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
        System.out.println("История: " + manager.getHistory());

        // Изменение статусов (оригинальные вызовы)
        Subtask s1 = manager.getSubtaskById(subtask1.getId());
        s1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(s1);

        Subtask s2 = manager.getSubtaskById(subtask2.getId());
        s2.setStatus(Status.DONE);
        manager.updateSubtask(s2);

        Subtask s3 = manager.getSubtaskById(subtask3.getId());
        s3.setStatus(Status.DONE);
        manager.updateSubtask(s3);

        // Проверка статусов эпиков (оригинальные сообщения)
        System.out.println("\n--- После обновления статусов ---");
        System.out.println("Эпик 1: " + manager.getEpicById(epic1.getId()));
        System.out.println("Эпик 2: " + manager.getEpicById(epic2.getId()));
        System.out.println("История: " + manager.getHistory());

        // Удаление задачи, которая есть в истории (новая проверка)
        System.out.println("\n=== Удаление задачи из истории ===");
        System.out.println("Удаляем задачу 1 (ID: " + task1.getId() + ")");
        manager.deleteTask(task1.getId());
        System.out.println("История после удаления: " + manager.getHistory());

        // Проверяем, что задачи больше нет в истории
        boolean taskInHistory = manager.getHistory().stream()
                .anyMatch(task -> task.getId() == task1.getId());
        System.out.println("Задача в истории: " + (taskInHistory ? "ДА" : "НЕТ"));

        // Удаление эпика с тремя подзадачами (новая проверка)
        System.out.println("\n=== Удаление эпика с подзадачами ===");
        System.out.println("Удаляем эпик 1 (ID: " + epic1.getId() + ") с тремя подзадачами");
        manager.deleteEpic(epic1.getId());
        System.out.println("История после удаления эпика: " + manager.getHistory());

        // Проверяем, что эпик и его подзадачи удалены из истории
        boolean epicInHistory = manager.getHistory().stream()
                .anyMatch(task -> task.getId() == epic1.getId());
        boolean subtask1InHistory = manager.getHistory().stream()
                .anyMatch(task -> task.getId() == subtask1.getId());
        boolean subtask2InHistory = manager.getHistory().stream()
                .anyMatch(task -> task.getId() == subtask2.getId());
        boolean subtask3InHistory = manager.getHistory().stream()
                .anyMatch(task -> task.getId() == subtask3.getId());

        System.out.println("Эпик в истории: " + (epicInHistory ? "ДА" : "НЕТ"));
        System.out.println("Подзадача 1 в истории: " + (subtask1InHistory ? "ДА" : "НЕТ"));
        System.out.println("Подзадача 2 в истории: " + (subtask2InHistory ? "ДА" : "НЕТ"));
        System.out.println("Подзадача 3 в истории: " + (subtask3InHistory ? "ДА" : "НЕТ"));

        // Оригинальное удаление задач
        System.out.println("\n--- После удаления ---");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
        System.out.println("История: " + manager.getHistory());
    }

    // Вспомогательный метод для проверки дубликатов
    private static void checkForDuplicates(TaskManager manager) {
        List<Task> history = manager.getHistory();
        Set<Integer> ids = new HashSet<>();
        boolean hasDuplicates = false;

        for (Task task : history) {
            if (!ids.add(task.getId())) {
                hasDuplicates = true;
                break;
            }
        }

        System.out.println("Есть повторения: " + (hasDuplicates ? "ДА" : "нет"));
        System.out.println("---");
    }
}
