package task;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicWithoutSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    void copyConstructor() {
        Epic original = new Epic("Original", "Description");
        original.setId(1);
        original.addSubtaskId(2);
        original.addSubtaskId(3);

        Epic copy = new Epic(original);

        assertEquals(original.getId(), copy.getId(), "ID должны совпадать");
        assertEquals(original.getTitle(), copy.getTitle(), "Названия должны совпадать");
        assertEquals(original.getDescription(), copy.getDescription(), "Описания должны совпадать");
        assertEquals(original.getSubtaskIds(), copy.getSubtaskIds(), "Списки подзадач должны совпадать");
    }
}