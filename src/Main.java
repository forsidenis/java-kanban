public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создание задач
        Task task1 = new Task("Помыть посуду", "Срочная задача");
        Task task2 = new Task("Сделать ДЗ", "По программированию");
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпиков с подзадачами
        Epic epic1 = new Epic("Организовать праздник", "Семейное торжество");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Купить продукты", "", epic1.getId());
        Subtask subtask2 = new Subtask("Пригласить гостей", "", epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic epic2 = new Epic("Купить квартиру", "Недвижимость");
        manager.createEpic(epic2);
        Subtask subtask3 = new Subtask("Выбрать район", "", epic2.getId());
        manager.createSubtask(subtask3);

        // Вывод всех задач
        System.out.println("--- Все задачи ---");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        // Изменение статусов
        Subtask s1 = manager.getSubtaskById(subtask1.getId());
        s1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(s1);

        Subtask s2 = manager.getSubtaskById(subtask2.getId());
        s2.setStatus(Status.DONE);
        manager.updateSubtask(s2);

        Subtask s3 = manager.getSubtaskById(subtask3.getId());
        s3.setStatus(Status.DONE);
        manager.updateSubtask(s3);

        // Проверка статусов эпиков
        System.out.println("\n--- После обновления статусов ---");
        System.out.println("Эпик 1: " + manager.getEpicById(epic1.getId()));
        System.out.println("Эпик 2: " + manager.getEpicById(epic2.getId()));

        // Удаление задач
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\n--- После удаления ---");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
    }
}
