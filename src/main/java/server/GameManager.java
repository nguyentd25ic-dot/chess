package server;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {
    private final Queue<ClientHandler>         waitingQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, GameRoom>        activeRooms  = new ConcurrentHashMap<>();
    private final Map<ClientHandler, GameRoom> playerRoom   = new ConcurrentHashMap<>();
    private final AtomicInteger                roomCounter  = new AtomicInteger(0);

    public synchronized void addToQueue(ClientHandler player) {
        if (waitingQueue.contains(player)) return;
        waitingQueue.offer(player);
        tryMatch();
    }

    public synchronized void removeFromQueue(ClientHandler player) {
        waitingQueue.remove(player);
    }

    private void tryMatch() {
        if (waitingQueue.size() < 2) return;
        ClientHandler white = waitingQueue.poll();
        ClientHandler black = waitingQueue.poll();
        String id = "room-" + roomCounter.incrementAndGet();
        GameRoom room = new GameRoom(id, white, black);
        activeRooms.put(id, room);
        playerRoom.put(white, room);
        playerRoom.put(black, room);
        room.startGame();
    }

    public void removePlayer(ClientHandler player) {
        waitingQueue.remove(player);
        GameRoom room = playerRoom.remove(player);
        if (room != null) {
            room.handleDisconnect(player);
            if (room.isGameOver()) activeRooms.remove(room.getRoomId());
        }
    }

    public GameRoom getRoomOf(ClientHandler player) {
        return playerRoom.get(player);
    }
}