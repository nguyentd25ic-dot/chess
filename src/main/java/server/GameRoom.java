package server;

import common.*;

public class GameRoom {
    private final String        roomId;
    private final ClientHandler whitePlayer;
    private final ClientHandler blackPlayer;
    private final ChessBoard    board    = new ChessBoard();
    private       boolean       gameOver = false;

    public GameRoom(String roomId, ClientHandler white, ClientHandler black) {
        this.roomId      = roomId;
        this.whitePlayer = white;
        this.blackPlayer = black;
    }

    public void startGame() {
        whitePlayer.send(new Message(MessageType.GAME_START, "server",
                blackPlayer.getUsername(), "white"));
        blackPlayer.send(new Message(MessageType.GAME_START, "server",
                whitePlayer.getUsername(), "black"));
    }

    public synchronized void handleMove(ClientHandler sender, String moveData) {
        if (gameOver || moveData.length() < 4) return;

        try {
            int fromC = moveData.charAt(0)-'a', fromR = 8-(moveData.charAt(1)-'0');
            int toC   = moveData.charAt(2)-'a', toR   = 8-(moveData.charAt(3)-'0');

            System.out.println("[GameRoom] Nhan move: " + moveData
                    + " fromR=" + fromR + " fromC=" + fromC
                    + " toR="   + toR   + " toC="   + toC);

            String result = board.makeMove(fromR, fromC, toR, toC);
            System.out.println("[GameRoom] Result: " + result);

            if (result.equals("invalid")) {
                sender.send(new Message(MessageType.MOVE_INVALID, "server", moveData));
                return;
            }

            broadcast(new Message(MessageType.MOVE_OK, "server", moveData, result));
            System.out.println("[GameRoom] Da broadcast MOVE_OK");
            if (result.equals("checkmate")) {
                gameOver = true;
                String winner =(sender==whitePlayer) ? "white" : "black";
                String winnerName= (sender==whitePlayer)
                        ? whitePlayer.getUsername() :  blackPlayer.getUsername();
                broadcast(new Message(MessageType.CHECKMATE, "server", winnerName));
                endGame(winner);
            }else if(result.equals("stalemate")) {
                gameOver = true;
                broadcast(new Message(MessageType.DRAW,"server",""));
                endGame("stalemate");
            }else if(result.startsWith("promotion")) {
                sender.send(new Message(MessageType.PROMOTE_REQUEST,"server",result));

            }
        } catch (Exception e) {
            System.err.println("[GameRoom] Loi handleMove: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public synchronized void handlePromotion(ClientHandler sender, String data) {
        try{
            String[] parts=data.split(",");
            int r=Integer.parseInt(parts[0]);
            int c=Integer.parseInt(parts[1]);
            Piece.Type type = Piece.Type.valueOf(parts[2]);
            board.promote(r,c,type);

            broadcast(new Message(MessageType.PROMOTE_RESPONSE,"server",data));

            Piece.Color next=board.getCurrentTurn();
            if (board.isCheckmate(next)) {
                gameOver = true;
                String winnerName = (sender == whitePlayer)
                        ? whitePlayer.getUsername()
                        : blackPlayer.getUsername();
                String winner = (sender == whitePlayer) ? "white" : "black";
                broadcast(new Message(MessageType.CHECKMATE, "server", winnerName));
                endGame(winner);
            } else if (board.isStalemate(next)) {
                gameOver = true;
                broadcast(new Message(MessageType.DRAW, "server", ""));
                endGame("draw");
            }
        }catch(Exception e){
            System.err.println("[GameRoom] Loi handlePromotion: " + e.getMessage());
        }
    }
    public synchronized void handleResign(ClientHandler sender) {
        if (gameOver) return;
        gameOver = true;
        String winner = (sender == whitePlayer) ? "black" : "white";
        broadcast(new Message(MessageType.RESIGN, "server", sender.getUsername()));
        endGame(winner);
    }

    public synchronized void handleDisconnect(ClientHandler sender) {
        if (gameOver) return;
        gameOver = true;
        getOpponent(sender).send(
                new Message(MessageType.OPPONENT_DISCONNECTED, "server", ""));
        endGame((sender == whitePlayer) ? "black" : "white");
    }

    public void handleChat(ClientHandler sender, String text) {
        getOpponent(sender).send(
                new Message(MessageType.CHAT, sender.getUsername(), text));
    }

    private void broadcast(Message msg) {
        whitePlayer.send(msg);
        blackPlayer.send(msg);
    }

    private void endGame(String winner) {
        Database.saveGame(
                whitePlayer.getUsername(),
                blackPlayer.getUsername(),
                winner,
                board.getMoveAsString()
        );
    }

    private ClientHandler getOpponent(ClientHandler p) {
        return (p == whitePlayer) ? blackPlayer : whitePlayer;
    }

    public boolean isGameOver() { return gameOver; }
    public String  getRoomId()  { return roomId; }
}