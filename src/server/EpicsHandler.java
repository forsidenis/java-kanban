package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import exceptions.NotFoundException;
import task.Epic;
import task.Subtask;
import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("=== EpicsHandler ===");
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
            System.out.println("Ошибка в EpicsHandler: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        System.out.println("Обработка GET для пути: " + path);

        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            System.out.println("Найдено эпиков: " + epics.size());
            String response = gson.toJson(epics);
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            Integer id = extractId(path);
            if (id == null) {
                sendBadRequest(exchange, "Некорректный ID эпика");
                return;
            }

            try {
                Epic epic = taskManager.getEpicById(id);
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            String[] pathParts = path.split("/");
            if (pathParts.length < 4) {
                sendBadRequest(exchange, "Некорректный путь");
                return;
            }

            try {
                Integer epicId = Integer.parseInt(pathParts[2]);
                List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epicId);
                System.out.println("Найдено подзадач для эпика " + epicId + ": " + subtasks.size());
                String response = gson.toJson(subtasks);
                sendSuccess(exchange, response);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID эпика");
            }
        } else {
            sendNotFound(exchange, "Ресурс не найден");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        System.out.println("Тело POST запроса: " + body);

        try {
            Epic epic = gson.fromJson(body, Epic.class);
            System.out.println("Десериализованный эпик: " + epic);

            if (epic.getId() == 0) {
                taskManager.createEpic(epic);
                String response = gson.toJson(epic);
                System.out.println("Создан новый эпик с ID: " + epic.getId());
                sendCreated(exchange, response);
            } else {
                taskManager.updateEpic(epic);
                System.out.println("Обновлен эпик с ID: " + epic.getId());
                sendCreated(exchange, "Эпик обновлен");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка парсинга JSON: " + e.getMessage());
            sendBadRequest(exchange, "Некорректный JSON: " + e.getMessage());
        } catch (NotFoundException e) {
            System.out.println("Эпик не найден: " + e.getMessage());
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        System.out.println("Обработка DELETE для пути: " + path);

        if (path.matches("/epics/\\d+")) {
            Integer id = extractId(path);
            if (id == null) {
                sendBadRequest(exchange, "Некорректный ID эпика");
                return;
            }

            try {
                taskManager.deleteEpic(id);
                System.out.println("Удален эпик с ID: " + id);
                sendSuccess(exchange, "Эпик удален");
            } catch (NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            }
        } else {
            sendNotFound(exchange, "Ресурс не найден");
        }
    }
}