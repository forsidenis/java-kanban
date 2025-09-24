package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class TasksHandlerTest extends BaseHttpTest {

    @Test
    protected void testGetTasks() throws Exception {
        // Создаем задачу без времени (чтобы избежать проблем с сериализацией)
        Task task = new Task("Test Task", "Description");
        taskManager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    protected void testGetTaskById() throws Exception {
        Task task = new Task("Test Task", "Description");
        taskManager.createTask(task);
        int taskId = task.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    protected void testCreateTask() throws Exception {
        // Создаем задачу без сложных временных полей для теста
        Task task = new Task("New Task", "Description");
        task.setStatus(Status.IN_PROGRESS);

        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    protected void testUpdateTask() throws Exception {
        Task task = new Task("Original Task", "Description");
        taskManager.createTask(task);

        task.setTitle("Updated Task");
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals("Updated Task", taskManager.getTaskById(task.getId()).getTitle());
    }

    @Test
    protected void testDeleteTask() throws Exception {
        Task task = new Task("Task to delete", "Description");
        taskManager.createTask(task);
        int taskId = task.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    protected void testTaskTimeOverlap() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofHours(1));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusMinutes(30)); // Пересекается с task1
        task2.setDuration(Duration.ofHours(1));

        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("пересекается"));
    }

    @Test
    protected void testCreateTaskWithTime() throws Exception {
        // Тест с временными параметрами
        Task task = new Task("Task with time", "Description");
        task.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        task.setDuration(Duration.ofHours(2));

        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());

        Task createdTask = taskManager.getAllTasks().get(0);
        assertNotNull(createdTask.getStartTime());
        assertNotNull(createdTask.getDuration());
    }
}