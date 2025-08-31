package task;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void subtaskEqualityById() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 1);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Subtask 2", "Different description", 2);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
    }

    @Test
    public void copyConstructor() {
        Subtask original = new Subtask("Original", "Description", 1);
        original.setId(2);
        original.setStatus(Status.DONE);

        Subtask copy = new Subtask(original);

        assertEquals(original.getId(), copy.getId(), "ID должны совпадать");
        assertEquals(original.getTitle(), copy.getTitle(), "Названия должны совпадать");
        assertEquals(original.getDescription(), copy.getDescription(), "Описания должны совпадать");
        assertEquals(original.getStatus(), copy.getStatus(), "Статусы должны совпадать");
        assertEquals(original.getEpicId(), copy.getEpicId(), "ID эпиков должны совпадать");
    }
}