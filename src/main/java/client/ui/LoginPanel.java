package client.ui;

import client.GameController;
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    public LoginPanel(GameController controller) {
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        JLabel title = new JLabel("♟ Chess Online");
        title.setFont(new Font("Serif", Font.BOLD, 36));
        title.setForeground(new Color(255,215,0));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        add(title, gbc);

        JLabel lbl = new JLabel("Tên người chơi:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridy=1; gbc.gridwidth=1; gbc.gridx=0;
        add(lbl, gbc);

        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        gbc.gridx=1;
        add(nameField, gbc);

        JButton btn = new JButton("Vào game");
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(new Color(255,215,0));
        btn.setForeground(Color.BLACK);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2;
        add(btn, gbc);

        Runnable doLogin = () -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this,"Nhập tên đi!"); return; }
            controller.login(name);
        };
        btn.addActionListener(e -> doLogin.run());
        nameField.addActionListener(e -> doLogin.run());
    }
}