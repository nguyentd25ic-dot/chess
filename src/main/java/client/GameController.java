package client;

import common.*;
import client.ui.*;
import javax.swing.SwingUtilities;

public class GameController implements Client.MessageListener {
    private final Client    client = new Client();
    private final MainFrame frame;
    private String          myUsername;
    private Piece.Color     myColor;

    public GameController() {
        frame = new MainFrame(this);
    }

    public void start() {
        if (!client.connect(this)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Không kết nối được server!");
            return;
        }
        frame.showLogin();
    }

    // ── Gửi lên server ───────────────────────────────────
    public void login(String username) {
        myUsername = username;
        client.send(new Message(MessageType.LOGIN, username, username));
    }

    public void findGame()        { client.send(new Message(MessageType.FIND_GAME,        myUsername, "")); }
    public void cancelFind()      { client.send(new Message(MessageType.CANCEL_FIND_GAME,      myUsername, "")); }
    public void sendMove(String move) {
        Message msg = new Message(MessageType.MOVE, myUsername, move);
        System.out.println("[SendMove] type=" + msg.getType()
                + " sender=" + msg.getSender()
                + " data=" + msg.getData());
        client.send(msg);
    }
    public void sendPromote(int r,int c,Piece.Type type) {
        String data=r+","+c+","+type.name();
        client.send(new Message(MessageType.PROMOTE_RESPONSE,myUsername,data));
    }
    public void resign()          { client.send(new Message(MessageType.RESIGN,           myUsername, "")); }
    public void sendChat(String t){ client.send(new Message(MessageType.CHAT,             myUsername, t)); }
    public void requestHistory()  { client.send(new Message(MessageType.HISTORY_REQUEST,  myUsername, "")); }
    public void requestStats()    { client.send(new Message(MessageType.STATS_REQUEST,    myUsername, "")); }

    // ── Nhận từ server ────────────────────────────────────
    @Override
    public void onMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case LOGIN_OK   -> frame.showLobby(myUsername);
                case LOGIN_FAIL -> frame.showError("Tên không hợp lệ!");
                case GAME_START -> {
                    myColor = msg.getExtra().equals("white")
                            ? Piece.Color.WHITE : Piece.Color.BLACK;
                    frame.showBoard(msg.getData(), myColor);
                }
                case MOVE_OK -> {
                    System.out.println("[Client] Nhan MOVE_OK data="
                            + msg.getData() + " extra=" + msg.getExtra());
                    frame.getBoardPanel().applyMove(msg.getData(), msg.getExtra());
                }
                case MOVE_INVALID -> {
                    System.out.println("[Client] Nhan MOVE_INVALID");
                    frame.getBoardPanel().onInvalidMove();
                }
                case PROMOTE_REQUEST -> {

                        frame.getBoardPanel().handlePromoteRequest(msg.getData());

                }
                case PROMOTE_RESPONSE -> {
                    String[] parts = msg.getData().split(",");
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);
                    Piece.Type type = Piece.Type.valueOf(parts[2]);
                    frame.getBoardPanel().applyPromote(r,c,type);
                }
                case CHECKMATE -> {
                    // msg.getData() = tên người thắng
                    boolean iWon = msg.getData().equals(myUsername);
                    frame.showGameOver(iWon
                            ? "Ban thang! Doi thu bi chieu bi!"
                            : "Ban thua! Ban bi chieu bi!");
                }
                case DRAW                 -> frame.showGameOver("Hòa cờ thế!");
                case RESIGN               -> frame.showGameOver(msg.getSender() + " đã đầu hàng!");
                case OPPONENT_DISCONNECTED-> frame.showGameOver("Đối thủ mất kết nối!");
                case CHAT                 -> frame.getBoardPanel().addChat(msg.getSender()+": "+msg.getData());
                case HISTORY_RESPONSE     -> frame.showHistory(msg.getData());
                case STATS_RESPONSE       -> frame.updateStats(msg.getData());
            }
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> frame.showError("Mất kết nối server!"));
    }

    public String      getUsername() { return myUsername; }
    public Piece.Color getMyColor()  { return myColor; }
}