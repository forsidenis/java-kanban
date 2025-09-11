package manager;

import exceptions.ManagerSaveException;
import task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private boolean isLoading = false;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
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

    protected void save() {
        if (isLoading) {
            return;
        }
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic,duration,startTime\n");
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
        TaskType type = task.getType();
        String epicId = type == TaskType.SUBTASK ? Integer.toString(((Subtask) task).getEpicId()) : "";
        String duration = task.getDuration() != null ? Long.toString(task.getDuration().toMinutes()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "";

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                duration,
                startTime);
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

        // Объявляем все переменные до циклов
        String line;
        boolean isHistorySection = false;
        int maxId = 0;
        Task task = null;
        List<Integer> historyIds = new ArrayList<>();
        String[] fields = null;
        int id = 0;
        TaskType type = null;
        Integer idForHistory = null;
        Subtask subtask = null;
        Epic epic = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isHistorySection = true;
                    continue;
                }

                if (isHistorySection) {
                    historyIds = historyFromString(line);
                    for (int i = 0; i < historyIds.size(); i++) {
                        idForHistory = historyIds.get(i);
                        if (manager.tasks.containsKey(idForHistory)) {
                            manager.historyManager.add(manager.tasks.get(idForHistory));
                        } else if (manager.epics.containsKey(idForHistory)) {
                            manager.historyManager.add(manager.epics.get(idForHistory));
                        } else if (manager.subtasks.containsKey(idForHistory)) {
                            manager.historyManager.add(manager.subtasks.get(idForHistory));
                        }
                    }
                } else if (!line.startsWith("id")) {
                    fields = line.split(",");
                    id = Integer.parseInt(fields[0]);
                    type = TaskType.valueOf(fields[1]);
                    task = fromString(line);

                    if (id > maxId) {
                        maxId = id;
                    }

                    switch (type) {
                        case EPIC:
                            manager.epics.put(id, (Epic) task);
                            break;
                        case SUBTASK:
                            manager.subtasks.put(id, (Subtask) task);
                            break;
                        default:
                            manager.tasks.put(id, task);
                    }
                }
            }
            manager.nextId = maxId + 1;

            // Восстанавливаем связи подзадач в эпиках
            for (int subtaskId : manager.subtasks.keySet()) {
                subtask = manager.subtasks.get(subtaskId);
                epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }

            // Обновляем статусы и время эпиков
            Epic epicItem = null;
            for (int epicId : manager.epics.keySet()) {
                epicItem = manager.epics.get(epicId);
                manager.updateEpicStatus(epicItem.getId());
                manager.updateEpicTime(epicItem.getId());
            }

            // Восстанавливаем prioritizedTasks
            for (Task t : manager.tasks.values()) {
                if (t.getStartTime() != null) {
                    manager.prioritizedTasks.add(t);
                }
            }
            for (Subtask s : manager.subtasks.values()) {
                if (s.getStartTime() != null) {
                    manager.prioritizedTasks.add(s);
                }
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

        Duration duration = null;
        if (fields.length > 6 && !fields[6].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(fields[6]));
        }

        LocalDateTime startTime = null;
        if (fields.length > 7 && !fields[7].isEmpty()) {
            startTime = LocalDateTime.parse(fields[7]);
        }

        switch (type) {
            case TASK:
                return new Task(id, title, description, status, duration, startTime);
            case EPIC:
                return new Epic(id, title, description, status, duration, startTime);
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new Subtask(id, title, description, status, epicId, duration, startTime);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> historyIds = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            String[] idStrings = value.split(",");
            for (String idString : idStrings) {
                historyIds.add(Integer.parseInt(idString));
            }
        }
        return historyIds;
    }
}