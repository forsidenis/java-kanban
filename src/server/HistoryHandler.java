package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;
import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("=== HistoryHandler ===");
        System.out.println("Метод: " + exchange.getRequestMethod());
        System.out.println("Путь: " + exchange.getRequestURI().getPath());

        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendBadRequest(exchange, "Неподдерживаемый метод: " + exchange.getRequestMethod());
                return;
            }

            List<Task> history = taskManager.getHistory();
            System.out.println("Размер истории: " + history.size());
            String response = gson.toJson(history);
            sendSuccess(exchange, response);

        } catch (Exception e) {
            System.out.println("Ошибка в HistoryHandler: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}