package client.ui;

import client.GameController;
import common.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardPanel extends JPanel {
    private static final int CELL = 70;
    private static final int SIZE = CELL * 8;

    private final GameController controller;
    private ChessBoard  board;
    private Piece.Color myColor;

    private int         selRow = -1, selCol = -1;
    private List<int[]> validMoves;

    private JLabel     statusLabel;
    private JTextArea  chatArea;
    private JTextField chatInput;

    public BoardPanel(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 30));
        loadImages();
        buildUI();
    }
    private final Map<String, Image> pieceImages = new HashMap<>();
    private void loadImages() {
        String[] colors = {"w", "b"};
        String[] names  = {"K", "Q", "R", "B", "N", "P"};
        for (String col : colors) {
            for (String name : names) {
                String key = col + name;
                try {
                    var url = getClass().getResource("/pieces/" + key + ".png");
                    System.out.println("[Images] " + key + " url=" + url);
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage()
                                .getScaledInstance(CELL - 8, CELL - 8, Image.SCALE_SMOOTH);
                        pieceImages.put(key, img);
                        System.out.println("[Images] OK: " + key);
                    } else {
                        System.err.println("[Images] NULL: " + key);
                    }
                } catch (Exception e) {
                    System.err.println("[Images] Loi: " + key + " - " + e.getMessage());
                }
            }
        }
        System.out.println("[Images] Tong loaded: " + pieceImages.size());
    }
    private void buildUI() {
        // ── Canvas bàn cờ ────────────────────────────────
        ChessCanvas canvas = new ChessCanvas();
        canvas.setPreferredSize(new Dimension(SIZE, SIZE));
        canvas.setMinimumSize(new Dimension(SIZE, SIZE));
        canvas.setMaximumSize(new Dimension(SIZE, SIZE));

        // Bọc canvas để căn giữa
        JPanel canvasWrapper = new JPanel(new GridBagLayout());
        canvasWrapper.setBackground(new Color(20, 20, 30));
        canvasWrapper.add(canvas);
        add(canvasWrapper, BorderLayout.CENTER);

        // ── Side panel ───────────────────────────────────
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(30, 30, 45));
        side.setPreferredSize(new Dimension(220, SIZE));
        side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Đang chờ...");
        statusLabel.setForeground(new Color(255, 215, 0));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(statusLabel);
        side.add(Box.createVerticalStrut(8));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(20, 20, 30));
        chatArea.setForeground(Color.LIGHT_GRAY);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(200, 400));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        side.add(scroll);
        side.add(Box.createVerticalStrut(5));

        chatInput = new JTextField();
        chatInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        chatInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatInput.addActionListener(e -> {
            String t = chatInput.getText().trim();
            if (!t.isEmpty()) {
                controller.sendChat(t);
                addChat("Tôi: " + t);
                chatInput.setText("");
            }
        });
        side.add(chatInput);
        side.add(Box.createVerticalStrut(10));

        JButton resignBtn = new JButton("Dau hang");
        resignBtn.setBackground(new Color(180, 40, 40));
        resignBtn.setForeground(Color.WHITE);
        resignBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        resignBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        resignBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resignBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Ban chac chan muon dau hang?", "Xac nhan",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) controller.resign();
        });
        side.add(resignBtn);
        add(side, BorderLayout.EAST);
    }

    // ── Public methods ────────────────────────────────────
    public void init(String opponent, Piece.Color myColor) {
        this.board   = new ChessBoard();
        this.myColor = myColor;
        selRow = -1; selCol = -1; validMoves = null;
        chatArea.setText("");
        addChat("--- Van moi bat dau ---");
        addChat("Ban: " + (myColor == Piece.Color.WHITE ? "TRANG" : "DEN"));
        addChat("Doi thu: " + opponent);
        updateStatus();
        repaint();
    }

    public void applyMove(String move, String result) {
        if (board == null || move.length() < 4) return;
        int fromC = move.charAt(0)-'a', fromR = 8-(move.charAt(1)-'0');
        int toC   = move.charAt(2)-'a', toR   = 8-(move.charAt(3)-'0');
        board.makeMove(fromR, fromC, toR, toC);
        selRow = -1; selCol = -1; validMoves = null;
        updateStatus();
        repaint();
    }

    public void onInvalidMove() {
        selRow = -1; selCol = -1; validMoves = null;
        repaint();
    }

    public void showGameOver(String msg) {
        statusLabel.setText("Ket thuc!");

        // Dùng SwingUtilities.invokeLater để đảm bảo dialog hiện sau khi repaint xong
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    msg + "\n\nBan muon lam gi?",
                    "Ket thuc van dau",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Tim tran moi", "Quay ve Lobby"},
                    "Quay ve Lobby"
            );

            // Reset trạng thái board
            board      = null;
            selRow     = -1;
            selCol     = -1;
            validMoves = null;
            repaint();

            // Lấy parent panel (CardLayout)
            JPanel parent = (JPanel) getParent();
            CardLayout cl = (CardLayout) parent.getLayout();

            if (choice == 0) {
                // Tìm trận mới
                cl.show(parent, "LOBBY");
                controller.findGame();
            } else {
                // Quay về lobby
                cl.show(parent, "LOBBY");
                controller.requestStats();
            }
        });
    }

    public void addChat(String text) {
        chatArea.append(text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void updateStatus() {
        if (board == null) return;
        boolean myTurn = board.getCurrentTurn() == myColor;
        statusLabel.setText(myTurn ? ">>> Luot cua ban" : "... Cho doi thu...");
        statusLabel.setForeground(myTurn
                ? new Color(100, 255, 100) : new Color(255, 165, 0));
    }

    // ── Chess Canvas ──────────────────────────────────────
    private class ChessCanvas extends JPanel {

        ChessCanvas() {
            setBackground(new Color(20, 20, 30));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { onClick(e); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawSquares(g2);
            drawHighlights(g2);
            drawPieces(g2);
            drawCoords(g2);
        }

        // ── Vẽ ô bàn cờ ──────────────────────────────────
        private void drawSquares(Graphics2D g) {
            Color light = new Color(240, 217, 181);
            Color dark  = new Color(181, 136,  99);
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    g.setColor((r + c) % 2 == 0 ? light : dark);
                    g.fillRect(c * CELL, r * CELL, CELL, CELL);
                }
            }
        }

        // ── Highlight ô đang chọn và nước đi hợp lệ ──────
        private void drawHighlights(Graphics2D g) {
            if (selRow >= 0) {
                int dr = displayRow(selRow);
                g.setColor(new Color(100, 200, 100, 160));
                g.fillRect(selCol * CELL, dr * CELL, CELL, CELL);
            }
            if (validMoves != null) {
                for (int[] m : validMoves) {
                    int dr = displayRow(m[0]);
                    boolean isCapture = board.getPiece(m[0], m[1]) != null;
                    g.setColor(isCapture
                            ? new Color(220, 60, 60, 160)
                            : new Color(80, 180, 80, 120));
                    g.fillRect(m[1] * CELL, dr * CELL, CELL, CELL);
                    // Chấm tròn
                    g.setColor(new Color(40, 140, 40, 200));
                    int dotSize = CELL / 3;
                    g.fillOval(m[1]*CELL + (CELL-dotSize)/2,
                            dr*CELL  + (CELL-dotSize)/2,
                            dotSize, dotSize);
                }
            }
        }

        // ── Vẽ quân cờ ───────────────────────────────────
        private void drawPieces(Graphics2D g) {
            System.out.println("[drawPieces] board=" + (board == null ? "NULL" : "OK"));
            if (board == null) return;

            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Piece p = board.getPiece(r, c);
                    if (p == null) continue;

                    int dr = displayRow(r);
                    String key = (p.getColor() == Piece.Color.WHITE ? "w" : "b")
                            + switch (p.getType()) {
                        case KING   -> "K";
                        case QUEEN  -> "Q";
                        case ROOK   -> "R";
                        case BISHOP -> "B";
                        case KNIGHT -> "N";
                        case PAWN   -> "P";
                    };
                    System.out.println("[drawPieces] ve " + key
                            + " tai (" + dr + "," + c + ")");
                    Image img = pieceImages.get(key);
                    if (img != null) {
                        g.drawImage(img,
                                c  * CELL + 4,
                                dr * CELL + 4,
                                CELL - 8,  // width
                                CELL - 8,  // height
                                this);
                    } else {
                        // Fallback chữ nếu không load được ảnh
                        g.setFont(new Font("SansSerif", Font.BOLD, CELL - 22));
                        FontMetrics fm = g.getFontMetrics();
                        String sym = key.substring(1);
                        int x = c  * CELL + (CELL - fm.stringWidth(sym)) / 2;
                        int y = dr * CELL + (CELL + fm.getAscent())       / 2 - 4;
                        g.setColor(p.getColor() == Piece.Color.WHITE
                                ? Color.WHITE : new Color(30, 30, 30));
                        g.drawString(sym, x, y);
                    }
                }
            }
        }

        // ── Tọa độ bàn cờ ────────────────────────────────
        private void drawCoords(Graphics2D g) {
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            for (int i = 0; i < 8; i++) {
                // Cột a-h
                int col = i;
                int row = (myColor == Piece.Color.WHITE) ? 7 : 0;
                g.setColor(((row + col) % 2 == 0)
                        ? new Color(181, 136, 99)
                        : new Color(240, 217, 181));
                g.drawString(String.valueOf((char)('a' + i)),
                        i * CELL + CELL - 12,
                        SIZE - 3);

                // Hàng 1-8
                int dispRow = i;
                int dispCol = (myColor == Piece.Color.WHITE) ? 0 : 7;
                g.setColor(((dispRow + dispCol) % 2 == 0)
                        ? new Color(181, 136, 99)
                        : new Color(240, 217, 181));
                g.drawString(
                        String.valueOf(myColor == Piece.Color.WHITE ? 8 - i : i + 1),
                        3,
                        i * CELL + 14);
            }
        }

        // ── Xử lý click ──────────────────────────────────
        private void onClick(MouseEvent e) {
            if (board == null || board.getCurrentTurn() != myColor) return;

            // Tính tọa độ thực từ pixel
            int dc = e.getX() / CELL;
            int dr = e.getY() / CELL;

            // Giới hạn trong bàn cờ
            if (dc < 0 || dc > 7 || dr < 0 || dr > 7) return;

            // Chuyển từ display row → logic row
            int r = (myColor == Piece.Color.WHITE) ? dr : (7 - dr);
            int c = dc;
            System.out.println("[Click] pixel=(" + e.getX() + "," + e.getY() + ")"
                    + " display=(" + dr + "," + dc + ")"
                    + " logic=(" + r + "," + c + ")");
            Piece clicked = board.getPiece(r, c);

            if (selRow < 0) {
                // Chưa chọn quân → chọn quân
                if (clicked != null && clicked.getColor() == myColor) {
                    selRow     = r;
                    selCol     = c;
                    validMoves = clicked.getValidMoves(board);
                    repaint();
                }
            } else {
                // Đã chọn quân
                if (clicked != null && clicked.getColor() == myColor) {
                    // Click vào quân khác của mình → đổi chọn
                    selRow     = r;
                    selCol     = c;
                    validMoves = clicked.getValidMoves(board);
                    repaint();
                    return;
                }
                // Gửi nước đi
                String move = ChessBoard.toAlgebratic(selRow, selCol)
                        + ChessBoard.toAlgebratic(r, c);
                System.out.println("[Move] selRow=" + selRow + " selCol=" + selCol
                        + " toR=" + r + " toC=" + c + " move=" + move);
                controller.sendMove(move);
                selRow = -1; selCol = -1; validMoves = null;
                repaint();
            }
        }

        // Lật hàng nếu chơi quân đen
        private int displayRow(int logicRow) {
            return (myColor == Piece.Color.WHITE) ? logicRow : (7 - logicRow);
        }
    }
    // Xử lý yêu cầu phong hậu từ server
    public void handlePromoteRequest(String data) {
        String[] parts = data.split(":")[1].split(",");
        int r = Integer.parseInt(parts[0]);
        int c = Integer.parseInt(parts[1]);

        SwingUtilities.invokeLater(() -> {
            String[] options = {"Hau (Q)", "Xe (R)", "Tinh (B)", "Ma (N)"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Chon quan phong hau:",
                    "Phong hau",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            Piece.Type type = switch (choice) {
                case 1  -> Piece.Type.ROOK;
                case 2  -> Piece.Type.BISHOP;
                case 3  -> Piece.Type.KNIGHT;
                default -> Piece.Type.QUEEN;
            };

            controller.sendPromote(r, c, type);
        });
    }


    // Áp dụng phong hậu lên board local
    public void applyPromote(int r, int c, Piece.Type type) {
        if (board == null) return;
        board.promote(r, c, type);
        repaint();
    }
    // ── Symbol quân cờ (dùng chữ để tránh lỗi font) ──────
    private static String symbol(Piece p) {
        return switch (p.getType()) {
            case KING   -> "K";
            case QUEEN  -> "Q";
            case ROOK   -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN   -> "P";
        };
    }
}