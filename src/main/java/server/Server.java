package server;

import java.io.IOException;
import java.net.*;

public class Server {
    private static final int PORT = 6000;

    public static void main(String[] args) throws IOException {
        // Khởi tạo Hibernate (tự tạo bảng nếu chưa có)
        HibernateUtil.getSessionFactory();

        GameManager manager = new GameManager();
        System.out.println("[Server] Listening on port " + PORT + "...");

        try (ServerSocket ss = new ServerSocket(PORT)) {
            while (true) {
                Socket client = ss.accept();
                System.out.println("[Server] New connection: " + client.getInetAddress());
                new Thread(new ClientHandler(client, manager)).start();
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }
}