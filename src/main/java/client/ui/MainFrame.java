package client.ui;

import client.GameController;
import common.Piece;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final GameController controller;
    private final CardLayout     cards = new CardLayout();
    private final JPanel         root  = new JPanel(cards);

    private final LoginPanel   loginPanel;
    private final LobbyPanel   lobbyPanel;
    private final BoardPanel   boardPanel;
    private final HistoryPanel historyPanel;

    public MainFrame(GameController controller) {
        this.controller = controller;
        setTitle("♟ Chess Online");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        loginPanel   = new LoginPanel(controller);
        lobbyPanel   = new LobbyPanel(controller);
        boardPanel   = new BoardPanel(controller);
        historyPanel = new HistoryPanel(controller);

        root.add(loginPanel,   "LOGIN");
        root.add(lobbyPanel,   "LOBBY");
        root.add(boardPanel,   "BOARD");
        root.add(historyPanel, "HISTORY");

        add(root);
        setVisible(true);
    }

    public void showLogin()  { cards.show(root, "LOGIN"); }
    public void showLobby(String username) {
        lobbyPanel.setUsername(username);
        controller.requestStats();
        cards.show(root, "LOBBY");
    }
    public void showBoard(String opponent, Piece.Color myColor) {
        boardPanel.init(opponent, myColor);
        cards.show(root, "BOARD");
    }
    public void showHistory(String data) {
        historyPanel.loadData(data);
        cards.show(root, "HISTORY");
    }
    public void showGameOver(String msg)  { boardPanel.showGameOver(msg); }
    public void showError(String msg)     {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    public void updateStats(String stats) { lobbyPanel.updateStats(stats); }

    public BoardPanel   getBoardPanel()   { return boardPanel; }
    public HistoryPanel getHistoryPanel() { return historyPanel; }
}