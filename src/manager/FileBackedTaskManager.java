package manager;

import task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private boolean isLoading = false;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    private void save() {
        if (isLoading) return;
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
            writer.write("\n");
            writer.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }

    private String toString(Task task) {
        String type = TaskType.TASK.toString();
        String epicId = "";
        if (task instanceof Epic) {
            type = TaskType.EPIC.toString();
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK.toString();
            epicId = Integer.toString(((Subtask) task).getEpicId());
        }
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    private static String historyToString(HistoryManager manager) {
        List<String> ids = new ArrayList<>();
        for (Task task : manager.getHistory()) {
            ids.add(Integer.toString(task.getId()));
        }
        return String.join(",", ids);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.isLoading = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean isHistorySection = false;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isHistorySection = true;
                    continue;
                }
                if (isHistorySection) {
                    List<Integer> historyIds = historyFromString(line);
                    for (Integer id : historyIds) {
                        if (manager.tasks.containsKey(id)) {
                            manager.historyManager.add(manager.tasks.get(id));
                        } else if (manager.epics.containsKey(id)) {
                            manager.historyManager.add(manager.epics.get(id));
                        } else if (manager.subtasks.containsKey(id)) {
                            manager.historyManager.add(manager.subtasks.get(id));
                        }
                    }
                } else if (!line.startsWith("id")) {
                    Task task = fromString(line);
                    int id = task.getId();
                    if (id > maxId) maxId = id;
                    if (task instanceof Epic) {
                        manager.epics.put(id, (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(id, (Subtask) task);
                    } else {
                        manager.tasks.put(id, task);
                    }
                }
            }
            manager.nextId = maxId + 1;

            // Восстанавливаем связи подзадач в эпиках
            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }

            // Обновляем статусы эпиков
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }
        manager.isLoading = false;
        return manager;
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        switch (type) {
            case TASK:
                return new Task(id, title, description, status);
            case EPIC:
                return new Epic(id, title, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new Subtask(id, title, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> historyIds = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            for (String id : value.split(",")) {
                historyIds.add(Integer.parseInt(id));
            }
        }
        return historyIds;
    }
}