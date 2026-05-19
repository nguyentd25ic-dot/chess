package client.ui;

import client.GameController;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class HistoryPanel extends JPanel {
    private final DefaultTableModel model;
    private final GameController    controller;

    public HistoryPanel(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(new Color(20,20,30));

        JLabel title = new JLabel("📋 Lịch sử ván đấu", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(new Color(255,215,0));
        title.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        add(title, BorderLayout.NORTH);

        String[] cols = {"#","Trắng","Đen","Kết quả","Số nước","Thời gian"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setBackground(new Color(30,30,45));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60,60,80));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowHorizontalLines(true);
        table.getTableHeader().setBackground(new Color(50,50,70));
        table.getTableHeader().setForeground(new Color(255,215,0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                setBackground(row%2==0 ? new Color(30,30,45) : new Color(40,40,60));
                setForeground(col==3
                        ? (v.toString().contains("Trắng") ? new Color(220,220,220)
                        : v.toString().contains("Đen")   ? new Color(100,200,255)
                        : new Color(255,200,0))
                        : Color.WHITE);
                return this;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton back = new JButton("← Quay lại Lobby");
        back.setBackground(new Color(70,130,180));
        back.setForeground(Color.WHITE);
        back.setFont(new Font("SansSerif", Font.BOLD, 14));
        back.addActionListener(e -> {
            JPanel root = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, this);
            // Quay về lobby thông qua MainFrame
            SwingUtilities.getWindowAncestor(this).repaint();
            controller.requestStats(); // refresh stats
            ((CardLayout)((JPanel)getParent()).getLayout()).show((JPanel)getParent(), "LOBBY");
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(new Color(20,20,30));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    public void loadData(String raw) {
        model.setRowCount(0);
        if (raw==null || raw.isBlank()) return;
        for (String line : raw.split("\n")) {
            String[] p = line.split("\\|");
            if (p.length>=6) {
                String result = p[3].equals("white") ? "⬜ Trắng thắng"
                        : p[3].equals("black") ? "⬛ Đen thắng"
                        : "🤝 Hòa";
                model.addRow(new Object[]{p[0],p[1],p[2],result,p[4],p[5]});
            }
        }
    }
}