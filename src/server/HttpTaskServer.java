package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault(), true); // По умолчанию с тестовыми данными
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this(taskManager, false); // Для тестов - без тестовых данных
    }

    public HttpTaskServer(TaskManager taskManager, boolean addTestData) throws IOException {
        this.taskManager = taskManager;
        this.gson = GsonFactory.createGson();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        if (addTestData) {
            addTestData();
        }
        configureRoutes();
    }

    private void addTestData() {
        try {
            System.out.println("Добавление тестовых данных...");

            // Создаем обычные задачи
            Task task1 = new Task("Помыть посуду", "Срочная задача");
            Task task2 = new Task("Сделать ДЗ", "По программированию");
            taskManager.createTask(task1);
            taskManager.createTask(task2);

            // Создаем эпики
            Epic epic1 = new Epic("Организовать праздник", "Семейное торжество");
            taskManager.createEpic(epic1);

            // Создаем подзадачи
            Subtask subtask1 = new Subtask("Купить продукты", "Для праздника", epic1.getId());
            Subtask subtask2 = new Subtask("Пригласить гостей", "Составить список", epic1.getId());
            Subtask subtask3 = new Subtask("Заказать торт", "Шоколадный", epic1.getId());
            taskManager.createSubtask(subtask1);
            taskManager.createSubtask(subtask2);
            taskManager.createSubtask(subtask3);

            // Создаем эпик без подзадач
            Epic epic2 = new Epic("Купить квартиру", "Недвижимость");
            taskManager.createEpic(epic2);

            // Меняем статусы для демонстрации
            subtask1.setStatus(Status.DONE);
            taskManager.updateSubtask(subtask1);

            task2.setStatus(Status.IN_PROGRESS);
            taskManager.updateTask(task2);

            // Просматриваем некоторые задачи для истории
            taskManager.getTaskById(task1.getId());
            taskManager.getEpicById(epic1.getId());
            taskManager.getSubtaskById(subtask1.getId());

            System.out.println("✅ Тестовые данные добавлены:");
            System.out.println("   - Задач: " + taskManager.getAllTasks().size());
            System.out.println("   - Эпиков: " + taskManager.getAllEpics().size());
            System.out.println("   - Подзадач: " + taskManager.getAllSubtasks().size());
            System.out.println("   - История: " + taskManager.getHistory().size() + " записей");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при добавлении тестовых данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureRoutes() {
        // Регистрируем обработчики для каждого эндпоинта
        server.createContext("/tasks", new TasksHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        server.createContext("/epics", new EpicsHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));

        System.out.println("✅ Маршруты настроены");
    }

    public void start() {
        server.start();
        System.out.println("==================================================");
        System.out.println("🚀 HTTP Task Server запущен на порту " + PORT);
        System.out.println("==================================================");
        System.out.println("Доступные эндпоинты:");
        System.out.println("• GET  http://localhost:8080/tasks");
        System.out.println("• GET  http://localhost:8080/tasks/{id}");
        System.out.println("• POST http://localhost:8080/tasks");
        System.out.println("• DELETE http://localhost:8080/tasks/{id}");
        System.out.println("• GET  http://localhost:8080/epics");
        System.out.println("• GET  http://localhost:8080/epics/{id}/subtasks");
        System.out.println("• GET  http://localhost:8080/history");
        System.out.println("• GET  http://localhost:8080/prioritized");
        System.out.println("==================================================");
    }

    public void stop() {
        server.stop(0);
        System.out.println("🛑 HTTP Task Server остановлен");
    }

    public static Gson getGson() {
        return GsonFactory.createGson();
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();

        // Добавляем обработчик для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nЗавершение работы сервера...");
            server.stop();
        }));

        // Бесконечный цикл чтобы сервер не завершался
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
