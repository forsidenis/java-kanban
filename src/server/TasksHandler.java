package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import exceptions.ManagerValidationException;
import exceptions.NotFoundException;
import task.Task;
import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("=== TasksHandler ===");
        System.out.println("Метод: " + exchange.getRequestMethod());
        System.out.println("Путь: " + exchange.getRequestURI().getPath());

        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendBadRequest(exchange, "Неподдерживаемый метод: " + method);
            }
        } catch (Exception e) {
            System.out.println("Ошибка в TasksHandler: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        System.out.println("Обработка GET для пути: " + path);

        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            System.out.println("Найдено задач: " + tasks.size());
            String response = gson.toJson(tasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            Integer id = extractId(path);
            if (id == null) {
                sendBadRequest(exchange, "Некорректный ID задачи");
                return;
            }

            try {
                Task task = taskManager.getTaskById(id);
                String response = gson.toJson(task);
                sendSuccess(exchange, response);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else {
            sendNotFound(exchange, "Ресурс не найден");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        System.out.println("Тело POST запроса: " + body);

        try {
            Task task = gson.fromJson(body, Task.class);
            System.out.println("Десериализованная задача: " + task);

            if (task.getId() == 0) {
                taskManager.createTask(task);
                String response = gson.toJson(task);
                System.out.println("Создана новая задача с ID: " + task.getId());
                sendCreated(exchange, response);
            } else {
                taskManager.updateTask(task);
                System.out.println("Обновлена задача с ID: " + task.getId());
                sendCreated(exchange, "Задача обновлена");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка парсинга JSON: " + e.getMessage());
            sendBadRequest(exchange, "Некорректный JSON: " + e.getMessage());
        } catch (ManagerValidationException e) {
            System.out.println("Ошибка валидации: " + e.getMessage());
            sendHasOverlaps(exchange, e.getMessage());
        } catch (NotFoundException e) {
            System.out.println("Задача не найдена: " + e.getMessage());
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        System.out.println("Обработка DELETE для пути: " + path);

        if (path.matches("/tasks/\\d+")) {
            Integer id = extractId(path);
            if (id == null) {
                sendBadRequest(exchange, "Некорректный ID задачи");
                return;
            }

            try {
                taskManager.deleteTask(id);
                System.out.println("Удалена задача с ID: " + id);
                sendSuccess(exchange, "Задача удалена");
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else {
            sendNotFound(exchange, "Ресурс не найден");
        }
    }
}