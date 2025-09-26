package server;

import java.io.IOException;

public class HttpServerRunner {
    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();

        System.out.println("Сервер запущен! Откройте браузер и перейдите по адресу:");
        System.out.println("http://localhost:8080/tasks");
        System.out.println("\nДля остановки сервера нажмите Ctrl+C");

        // Ожидаем завершения
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Сервер остановлен");
        }
    }
}
