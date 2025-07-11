package task;

import java.util.*;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
    }

    @Override
    public void setStatus(Status status) {
        throw new UnsupportedOperationException("Status of Epic is calculated automatically");
    }

    public void updateStatus(Status status) {
        super.setStatus(status);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
