package server;

import common.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket        socket;
    private final GameManager   manager;
    private ObjectOutputStream  out;
    private ObjectInputStream   in;
    private String              username;

    public ClientHandler(Socket socket, GameManager manager) {
        this.socket  = socket;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());
            while (true) handle((Message) in.readObject());
        } catch (Exception e) {
            System.out.println("[Server] Disconnected: " + username);
        } finally {
            manager.removePlayer(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handle(Message msg) {
        try {
            switch (msg.getType()) {
                case LOGIN -> {
                    username = msg.getData().trim();
                    boolean ok = Database.registerOrLogin(username);
                    send(new Message(ok ? MessageType.LOGIN_OK : MessageType.LOGIN_FAIL,
                            "server", username));
                }
                case FIND_GAME   -> manager.addToQueue(this);
                case CANCEL_FIND_GAME -> manager.removeFromQueue(this);
                case MOVE -> {
                    GameRoom room = manager.getRoomOf(this);
                    System.out.println("[Handler] MOVE data='" + msg.getData()
                            + "' sender='" + msg.getSender() + "'");
                    if (room != null) room.handleMove(this, msg.getData());
                    if (room != null) room.handleMove(this, msg.getData());

                }
                case PROMOTE_RESPONSE->{
                    GameRoom room = manager.getRoomOf(this);
                    if(room!=null) room.handlePromotion(this, msg.getData());
                }
                case RESIGN -> {
                    GameRoom room = manager.getRoomOf(this);
                    if (room != null) room.handleResign(this);
                }
                case CHAT -> {
                    GameRoom room = manager.getRoomOf(this);
                    if (room != null) room.handleChat(this, msg.getData());
                }
                case HISTORY_REQUEST -> {
                    List<String> history = Database.getHistory(username);
                    send(new Message(MessageType.HISTORY_RESPONSE, "server",
                            String.join("\n", history)));
                }
                case STATS_REQUEST -> {
                    send(new Message(MessageType.STATS_RESPONSE, "server",
                            Database.getStats(username)));
                }
            }
        } catch (Exception e) {
            // In ra lỗi cụ thể thay vì crash
            System.err.println("[Server] Loi xu ly message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void send(Message msg) {
        try { out.writeObject(msg); out.flush(); }
        catch (IOException e) { System.err.println("[Send error] " + e.getMessage()); }
    }

    public String getUsername() { return username; }
}