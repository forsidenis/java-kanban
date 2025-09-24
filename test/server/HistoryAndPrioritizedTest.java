package server;

import org.junit.jupiter.api.Test;
import task.Task;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

public class HistoryAndPrioritizedTest extends BaseHttpTest {

    @Test
    protected void testGetHistory() throws Exception {
        Task task = new Task("Test Task", "Description");
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId()); // Добавляем в историю

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    protected void testGetPrioritizedTasks() throws Exception {
        Task task = new Task("Test Task", "Description");
        taskManager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }
}