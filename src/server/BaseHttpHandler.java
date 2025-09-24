package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected static final Gson GSON = GsonFactory.createGson();

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        String response = GSON.toJson(new ErrorResponse(message));
        sendText(exchange, response, 404);
    }

    protected void sendHasOverlaps(HttpExchange exchange, String message) throws IOException {
        String response = GSON.toJson(new ErrorResponse(message));
        sendText(exchange, response, 406);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String response = GSON.toJson(new ErrorResponse(message));
        sendText(exchange, response, 400);
    }

    protected void sendInternalError(HttpExchange exchange, String message) throws IOException {
        String response = GSON.toJson(new ErrorResponse(message));
        sendText(exchange, response, 500);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected Integer extractId(String path) {
        try {
            String[] parts = path.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}