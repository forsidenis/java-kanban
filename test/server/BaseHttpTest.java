package server;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;

public abstract class BaseHttpTest {
    protected TaskManager taskManager;
    protected HttpTaskServer server;
    protected Gson gson;
    protected static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = new HttpTaskServer(taskManager);
        gson = GsonFactory.createGson(); // Используем фабрику
        server.start();

        // Даем серверу время на запуск
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop();
        }

        // Даем серверу время на остановку
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}