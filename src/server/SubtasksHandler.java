package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import exceptions.ManagerValidationException;
import exceptions.NotFoundException;
import task.Subtask;
import java.io.IOException;
import java.util.List;

        public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
            private final TaskManager taskManager;
            private final Gson gson;

            public SubtasksHandler(TaskManager taskManager, Gson gson) {
                this.taskManager = taskManager;
                this.gson = gson;
            }

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                System.out.println("=== SubtasksHandler ===");
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
                    System.out.println("Ошибка в SubtasksHandler: " + e.getMessage());
                    e.printStackTrace();
                    sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
                }
            }

            private void handleGet(HttpExchange exchange, String path) throws IOException {
                System.out.println("Обработка GET для пути: " + path);

                if (path.equals("/subtasks")) {
                    List<Subtask> subtasks = taskManager.getAllSubtasks();
                    System.out.println("Найдено подзадач: " + subtasks.size());
                    String response = gson.toJson(subtasks);
                    sendSuccess(exchange, response);
                } else if (path.matches("/subtasks/\\d+")) {
                    Integer id = extractId(path);
                    if (id == null) {
                        sendBadRequest(exchange, "Некорректный ID подзадачи");
                        return;
                    }

                    try {
                        Subtask subtask = taskManager.getSubtaskById(id);
                        String response = gson.toJson(subtask);
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
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    System.out.println("Десериализованная подзадача: " + subtask);

                    if (subtask.getId() == 0) {
                        taskManager.createSubtask(subtask);
                        String response = gson.toJson(subtask);
                        System.out.println("Создана новая подзадача с ID: " + subtask.getId());
                        sendCreated(exchange, response);
                    } else {
                        taskManager.updateSubtask(subtask);
                        System.out.println("Обновлена подзадача с ID: " + subtask.getId());
                        sendCreated(exchange, "Подзадача обновлена");
                    }
                } catch (JsonSyntaxException e) {
                    System.out.println("Ошибка парсинга JSON: " + e.getMessage());
                    sendBadRequest(exchange, "Некорректный JSON: " + e.getMessage());
                } catch (ManagerValidationException e) {
                    System.out.println("Ошибка валидации: " + e.getMessage());
                    sendHasOverlaps(exchange, e.getMessage());
                } catch (NotFoundException | IllegalArgumentException e) {
                    System.out.println("Подзадача не найдена: " + e.getMessage());
                    sendNotFound(exchange, e.getMessage());
                }
            }

            private void handleDelete(HttpExchange exchange, String path) throws IOException {
                System.out.println("Обработка DELETE для пути: " + path);

                if (path.matches("/subtasks/\\d+")) {
                    Integer id = extractId(path);
                    if (id == null) {
                        sendBadRequest(exchange, "Некорректный ID подзадачи");
                        return;
                    }

                    try {
                        taskManager.deleteSubtask(id);
                        System.out.println("Удалена подзадача с ID: " + id);
                        sendSuccess(exchange, "Подзадача удалена");
                    } catch (NotFoundException e) {
                        sendNotFound(exchange, e.getMessage());
                    }
                } else {
                    sendNotFound(exchange, "Ресурс не найден");
                }
            }
        }