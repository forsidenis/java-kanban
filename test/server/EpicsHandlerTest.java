package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Status;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class EpicsHandlerTest extends BaseHttpTest {
    private final Gson gson = new Gson();

    @Test
    protected void testGetEpicSubtasks() throws Exception {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        taskManager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Subtask"));
    }

    @Test
    protected void testEpicStatusCalculation() throws Exception {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        subtask.setStatus(Status.DONE);
        taskManager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("DONE"));
    }
}
