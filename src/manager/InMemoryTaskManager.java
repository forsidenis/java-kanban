package manager;

import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import exceptions.ManagerValidationException;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder()))
    );

    private int generateId() {
        return nextId++;
    }

    // Методы для задач
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
            prioritizedTasks.remove(tasks.get(id));
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(new Task(task));
        }
        return task;
    }

    @Override
    public void createTask(Task task) {
        validateTaskTime(task);
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (tasks.containsKey(updatedTask.getId())) {
            validateTaskTime(updatedTask);
            Task oldTask = tasks.get(updatedTask.getId());
            prioritizedTasks.remove(oldTask);
            tasks.put(updatedTask.getId(), updatedTask);
            if (updatedTask.getStartTime() != null) {
                prioritizedTasks.add(updatedTask);
            }
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(task);
        }
    }

    // Методы для эпиков
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (int id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Epic epic : epics.values()) {
            for (int subtaskId : epic.getSubtaskIds()) {
                historyManager.remove(subtaskId);
                prioritizedTasks.remove(subtasks.get(subtaskId));
            }
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(new Epic(epic));
        }
        return epic;
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        Epic existingEpic = epics.get(updatedEpic.getId());
        if (existingEpic != null) {
            existingEpic.setTitle(updatedEpic.getTitle());
            existingEpic.setDescription(updatedEpic.getDescription());
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
                prioritizedTasks.remove(subtasks.get(subtaskId));
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
            prioritizedTasks.remove(subtasks.get(id));
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(new Subtask(subtask));
        }
        return subtask;
    }

    @Override
    public void createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            throw new IllegalArgumentException("Epic with id " + epicId + " not found");
        }

        validateTaskTime(subtask);
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(epicId).addSubtaskId(subtask.getId());
        updateEpicStatus(epicId);
        updateEpicTime(epicId);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        int id = updatedSubtask.getId();
        Subtask existingSubtask = subtasks.get(id);

        if (existingSubtask == null) return;

        validateTaskTime(updatedSubtask);
        prioritizedTasks.remove(existingSubtask);

        int oldEpicId = existingSubtask.getEpicId();
        int newEpicId = updatedSubtask.getEpicId();

        if (oldEpicId == newEpicId) {
            subtasks.put(id, updatedSubtask);
            updateEpicStatus(oldEpicId);
            updateEpicTime(oldEpicId);
        } else if (epics.containsKey(newEpicId)) {
            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.removeSubtaskId(id);
                updateEpicStatus(oldEpicId);
                updateEpicTime(oldEpicId);
            }

            Epic newEpic = epics.get(newEpicId);
            if (newEpic != null) {
                subtasks.put(id, updatedSubtask);
                newEpic.addSubtaskId(id);
                updateEpicStatus(newEpicId);
                updateEpicTime(newEpicId);
            }
        }

        if (updatedSubtask.getStartTime() != null) {
            prioritizedTasks.add(updatedSubtask);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epicId);
                updateEpicTime(epicId);
            }
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            Status status = subtask.getStatus();
            if (status != Status.NEW) allNew = false;
            if (status != Status.DONE) allDone = false;
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    protected void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getSubtasksByEpicId(epicId);

        if (epicSubtasks.isEmpty()) {
            epic.setDuration(null);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
            if (subtask.getStartTime() != null) {
                if (earliestStartTime == null || subtask.getStartTime().isBefore(earliestStartTime)) {
                    earliestStartTime = subtask.getStartTime();
                }
            }
            if (subtask.getEndTime() != null) {
                if (latestEndTime == null || subtask.getEndTime().isAfter(latestEndTime)) {
                    latestEndTime = subtask.getEndTime();
                }
            }
        }

        epic.setDuration(totalDuration);
        epic.setStartTime(earliestStartTime);
        epic.setEndTime(latestEndTime);
    }

    private void validateTaskTime(Task task) {
        if (task.getStartTime() == null) {
            return;
        }

        boolean hasOverlap = prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .anyMatch(t -> isTimeOverlap(t, task));

        if (hasOverlap) {
            throw new ManagerValidationException("Задача пересекается по времени с существующей задачей");
        }
    }

    private boolean isTimeOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }
}