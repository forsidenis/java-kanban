package manager;

import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.*;

public class TaskManager {
    private int nextId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int generateId() {
        return nextId++;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            List<Integer> existingSubtaskIds = epics.get(epic.getId()).getSubtaskIds();
            epics.put(epic.getId(), epic);
            epic.getSubtaskIds().addAll(existingSubtaskIds);
            updateEpicStatus(epic.getId());
        }
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            throw new IllegalArgumentException("Epic with id " + epicId + " not found");
        }

        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(epicId).addSubtaskId(subtask.getId());
        updateEpicStatus(epicId);
    }

    public void updateSubtask(Subtask newSubtask) {
        int id = newSubtask.getId();
        Subtask oldSubtask = subtasks.get(id);

        if (oldSubtask == null) return;

        int oldEpicId = oldSubtask.getEpicId();
        int newEpicId = newSubtask.getEpicId();

        if (oldEpicId == newEpicId) {
            subtasks.put(id, newSubtask);
            updateEpicStatus(oldEpicId);
        } else if (epics.containsKey(newEpicId)) {
            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.removeSubtaskId(id);
                updateEpicStatus(oldEpicId);
            }

            subtasks.put(id, newSubtask);
            Epic newEpic = epics.get(newEpicId);
            newEpic.addSubtaskId(id);
            updateEpicStatus(newEpicId);
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epicId);
            }
        }
    }

    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
        }
        return result;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.updateStatus(Status.NEW);
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
            epic.updateStatus(Status.NEW);
        } else if (allDone) {
            epic.updateStatus(Status.DONE);
        } else {
            epic.updateStatus(Status.IN_PROGRESS);
        }
    }
}