package task;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void taskEqualityById() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);

        Task task2 = new Task("Task 2", "Different description");
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    public void copyConstructor() {
        Task original = new Task("Original", "Description");
        original.setId(1);
        original.setStatus(Status.IN_PROGRESS);

        Task copy = new Task(original);

        assertEquals(original.getId(), copy.getId(), "ID должны совпадать");
        assertEquals(original.getTitle(), copy.getTitle(), "Названия должны совпадать");
        assertEquals(original.getDescription(), copy.getDescription(), "Описания должны совпадать");
        assertEquals(original.getStatus(), copy.getStatus(), "Статусы должны совпадать");
    }
}