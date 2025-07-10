import java.util.*;
class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() { return subtaskIds; }
    public void addSubtaskId(int id) { subtaskIds.add(id); }
    public void removeSubtaskId(int id) { subtaskIds.remove((Integer) id); }
    public void clearSubtaskIds() { subtaskIds.clear(); }

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
