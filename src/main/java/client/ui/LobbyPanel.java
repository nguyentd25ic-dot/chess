package client.ui;

import client.GameController;
import javax.swing.*;
import java.awt.*;

public class LobbyPanel extends JPanel {
    private JLabel  welcomeLabel, statsLabel, statusLabel;
    private JButton findBtn;
    private boolean finding = false;

    public LobbyPanel(GameController controller) {
        setLayout(new GridBagLayout());
        setBackground(new Color(30,30,40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,12,12,12);

        welcomeLabel = new JLabel("Xin chào!");
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(255,215,0));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        add(welcomeLabel, gbc);

        statsLabel = new JLabel("Thắng: 0  |  Thua: 0  |  Hòa: 0");
        statsLabel.setForeground(Color.LIGHT_GRAY);
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy=1;
        add(statsLabel, gbc);

        findBtn = new JButton("🎮 Tìm đối thủ");
        findBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        findBtn.setBackground(new Color(46,139,87));
        findBtn.setForeground(Color.WHITE);
        findBtn.setPreferredSize(new Dimension(200,50));
        findBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy=2; gbc.gridwidth=1; gbc.gridx=0;
        add(findBtn, gbc);

        JButton histBtn = new JButton("📋 Lịch sử");
        histBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        histBtn.setBackground(new Color(70,130,180));
        histBtn.setForeground(Color.WHITE);
        histBtn.setPreferredSize(new Dimension(200,50));
        histBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx=1;
        add(histBtn, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(255,165,0));
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
        add(statusLabel, gbc);

        findBtn.addActionListener(e -> {
            finding = !finding;
            if (finding) {
                findBtn.setText("⏳ Đang tìm... (hủy)");
                findBtn.setBackground(new Color(180,60,60));
                statusLabel.setText("Đang chờ đối thủ...");
                controller.findGame();
            } else {
                findBtn.setText("🎮 Tìm đối thủ");
                findBtn.setBackground(new Color(46,139,87));
                statusLabel.setText(" ");
                controller.cancelFind();
            }
        });

        histBtn.addActionListener(e -> controller.requestHistory());
    }

    public void setUsername(String name) { welcomeLabel.setText("Xin chào, " + name + "!"); }
    public void updateStats(String stats) {
        String[] p = stats.split("\\|");
        if (p.length==3)
            statsLabel.setText("Thắng: "+p[0]+"  |  Thua: "+p[1]+"  |  Hòa: "+p[2]);
    }
}