package common;
import common.pieces.*;
import java.io.Serializable;
import java.util.*;
import common.Piece;
public class ChessBoard implements Serializable {
    private static final long serialVersionUID = 1L;

    private Piece[][] board= new Piece[8][8];
    private Piece.Color currentTurn = Piece.Color.WHITE;
    private int EnPassantRow=-1,EnPassantCol=-1;
    private List<String> moveHistory=new ArrayList<>();

    public ChessBoard() {
        setupInitialPosition();
    }
    private void setupInitialPosition() {
        Piece.Type[] back={
                Piece.Type.ROOK,
                Piece.Type.KNIGHT,
                Piece.Type.BISHOP,
                Piece.Type.QUEEN,
                Piece.Type.KING,
                Piece.Type.BISHOP,
                Piece.Type.KNIGHT,
                Piece.Type.ROOK
        };
        for (int c=0;c<8;c++) {
            board[0][c] = PieceFactory.createPiece(back[c],        Piece.Color.BLACK, 0, c);
            board[1][c] = PieceFactory.createPiece(Piece.Type.PAWN, Piece.Color.BLACK, 1, c);
            board[6][c] = PieceFactory.createPiece(Piece.Type.PAWN, Piece.Color.WHITE, 6, c);
            board[7][c] = PieceFactory.createPiece(back[c],        Piece.Color.WHITE, 7, c);
        }
    }
    public String makeMove(int fromR, int fromC, int toR, int toC) {
        Piece piece=board[fromR][fromC];
        System.out.println("[makeMove] piece=" + (piece==null ? "null" : piece.getType())
                + " color=" + (piece==null ? "null" : piece.getColor())
                + " currentTurn=" + currentTurn);
        if(piece==null|| piece.getColor()!=currentTurn) return "invalid";
        List<int[]> validMoves = piece.getValidMoves(this);
        System.out.println("[makeMove] validMoves count=" + validMoves.size());
        for (int[] m : validMoves) {
            System.out.println("  valid: " + toAlgebratic(m[0], m[1]));
        }
        int[]matched=null;
        for(int[]m: piece.getValidMoves(this))
            if (m[0]==toR && m[1]==toC) {matched=m;break;}
        if (matched==null) {
            System.out.println("[makeMove] Khong tim thay move den " + toAlgebratic(toR,toC));
            return "invalid";
        }

        EnPassantCol=-1;
        EnPassantRow=-1;
        if(piece.getType()==Piece.Type.PAWN&&Math.abs(toR-fromR)==2) {
            EnPassantRow=(fromR+toR)/2;
            EnPassantCol=fromC;
        }
        if(matched.length>2&&matched[2]==3){
            board[fromR][toC]=null;
        }
        // nhap thanh
        if(piece.getType()==Piece.Type.KING&&matched.length>2){
            if(matched[2]==1){
                Piece r= board[fromR][7];
                board[fromR][5]=r;
                board[fromR][7]=null;
                if(r!=null){
                    r.setRow(fromR);
                    r.setCol(5);
                    r.setMoved();
                }
            }
            else if (matched[2]==2){
                Piece r= board[fromR][0];
                board[fromR][3]=r;
                board[fromR][0]=null;
                if(r!=null){
                    r.setRow(fromR);
                    r.setCol(3);
                    r.setMoved();
                }
            }
        }
        movePiece(fromR,fromC,toR,toC);
        moveHistory.add(toAlgebratic(fromR,fromC)+toAlgebratic(toR,toC));
        //phong hau
        if(piece.getType()==Piece.Type.PAWN&&(toR==0||toR==7)){
            return "promotion:"+toR+","+toC;
        }
        currentTurn=(currentTurn==Piece.Color.WHITE)?Piece.Color.BLACK:Piece.Color.WHITE;
        if(isCheckmate(currentTurn)) return "checkmate";
        if(isStalemate(currentTurn))  return "stalemate";
        return "ok";

    }
    public void promote(int r, int c,Piece.Type type) {
        Piece p=board[r][c];
        if(p==null) return;
        board[r][c]=PieceFactory.createPiece(type,p.getColor(),r,c);
        currentTurn=(currentTurn==Piece.Color.WHITE)?Piece.Color.BLACK:Piece.Color.WHITE;
    }
    public void movePiece(int fromR, int fromC, int toR, int toC) {
        Piece p=board[fromR][fromC];
        if(p==null) return;
        board[toR][toC]=p;
        board[fromR][fromC]=null;
        p.setRow(toR);
        p.setCol(toC);
        p.setMoved();
    }
    public boolean isIncheck(Piece.Color color) {
        int[] king = findKing(color);
        if (king == null) return false;
        Piece.Color opp = (color == Piece.Color.WHITE)
                ? Piece.Color.BLACK : Piece.Color.WHITE;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p == null || p.getColor() != opp) continue;

                // KHÔNG gọi getValidMoves, chỉ dùng getPseudoMoves
                // và bỏ qua King đối thủ để tránh đệ quy
                if (p.getType() == Piece.Type.KING) {
                    // Kiểm tra King tấn công thủ công
                    int dr = Math.abs(r - king[0]);
                    int dc = Math.abs(c - king[1]);
                    if (dr <= 1 && dc <= 1) return true;
                    continue;
                }

                for (int[] m : p.getPseudoMoves(this)) {
                    if (m[0] == king[0] && m[1] == king[1]) return true;
                }
            }
        }
        return false;
    }
    public boolean isCheckmate(Piece.Color color) {
        return isIncheck(color) && !hasValidMove(color);
    }

    public boolean isStalemate(Piece.Color color) {
        return !isIncheck(color) && !hasValidMove(color);
    }

    private boolean hasValidMove(Piece.Color color) {
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = board[r][c];
            if (p!=null && p.getColor()==color && !p.getValidMoves(this).isEmpty())
                return true;
        }
        return false;
    }

    private int[] findKing(Piece.Color color) {
        for(int r=0;r<8;r++) for(int c=0;c<8;c++){
            Piece p=board[r][c];
            if(p!=null&&p.getColor()==color&&p.getType()==Piece.Type.KING)
                return new int[] {r,c};
        }
        return null;
    }
    public ChessBoard copy() {
        ChessBoard clone = new ChessBoard();
        for (int r=0;r<8;r++) for (int c=0;c<8;c++)
            clone.board[r][c] = (board[r][c]!=null) ? board[r][c].copy() : null;
        clone.currentTurn  = this.currentTurn;
        clone.EnPassantRow = this.EnPassantRow;
        clone.EnPassantCol = this.EnPassantCol;
        return clone;
    }
    public static String toAlgebratic(int r, int c) {
        return "" + (char)('a' + c) + (8 - r);
    }

    public Piece getPiece(int r, int c) {
        return board[r][c];
    }
    public Piece.Color getCurrentTurn() {
        return currentTurn;
    }
    public int getEnPassantRow() {
        return EnPassantRow;
    }
    public int getEnPassantCol() {
        return EnPassantCol;
    }
    public List<String> getMoveHistory() {
        return moveHistory;
    }
    public String getMoveAsString() {
        return String.join(",", moveHistory);
    }

}
