package client;

import common.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private static final String HOST = "localhost"; // ← đổi IP nếu server khác máy
    private static final int    PORT = 6000;

    private Socket             socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;
    private MessageListener    listener;

    public interface MessageListener {
        void onMessage(Message msg);
        void onDisconnected();
    }

    public boolean connect(MessageListener listener) {
        this.listener = listener;
        try {
            socket = new Socket(HOST, PORT);
            out    = new ObjectOutputStream(socket.getOutputStream());
            in     = new ObjectInputStream(socket.getInputStream());
            new Thread(this::listen).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void listen() {
        try {
            while (true) listener.onMessage((Message) in.readObject());
        } catch (Exception e) {
            listener.onDisconnected();
        }
    }

    public void send(Message msg) {
        try { out.writeObject(msg); out.flush(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void disconnect() {
        try { if (socket!=null) socket.close(); } catch (IOException ignored) {}
    }
}