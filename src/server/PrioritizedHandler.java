package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;
import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("=== PrioritizedHandler ===");
        System.out.println("Метод: " + exchange.getRequestMethod());
        System.out.println("Путь: " + exchange.getRequestURI().getPath());

        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendBadRequest(exchange, "Неподдерживаемый метод: " + exchange.getRequestMethod());
                return;
            }

            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
            System.out.println("Приоритетных задач: " + prioritizedTasks.size());
            String response = gson.toJson(prioritizedTasks);
            sendSuccess(exchange, response);

        } catch (Exception e) {
            System.out.println("Ошибка в PrioritizedHandler: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}