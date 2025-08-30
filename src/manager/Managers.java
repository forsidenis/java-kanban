package manager;

import java.io.File;

public class Managers {
    private static final File DEFAULT_STORAGE_FILE = new File("task_storage.csv");

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(DEFAULT_STORAGE_FILE);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
