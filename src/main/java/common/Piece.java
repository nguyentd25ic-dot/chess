package common;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
public abstract class Piece implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Color {
        WHITE, BLACK
    }
    public enum Type {
        KING,
        QUEEN,
        ROOK,
        KNIGHT,
        BISHOP,
        PAWN,
    }
    protected Color color;
    protected Type type;
    protected int row,col;
    protected boolean hasMoved = false;

    public Piece(Color color, Type type, int row, int col) {
        this.color = color;
        this.type = type;
        this.row = row;
        this.col = col;
    }
    public abstract List<int[]> getPseudoMoves(ChessBoard board);

    public List<int[]> getValidMoves(ChessBoard board){
        List<int[]> valid = new ArrayList<>();
        List<int[]> pseudo = getPseudoMoves(board);

        System.out.println("[getValidMoves] " + getType() + " " + getColor()
                + " at (" + row + "," + col + ")"
                + " pseudo=" + pseudo.size());
        for(int[] m: getPseudoMoves(board)){
            ChessBoard copy=board.copy();
            copy.movePiece(row,col,m[0],m[1]);
            boolean inCheck = copy.isIncheck(color);
            System.out.println("  thu move (" + m[0] + "," + m[1] + ") inCheck=" + inCheck);
            if(!copy.isIncheck(color)) valid.add(m);
        }
        System.out.println("  valid=" + valid.size());
        return valid;
    }
    protected boolean inBounds(int r, int c){
        return r >= 0 && r <8 && c >= 0 && c <8;
    }
    protected boolean addIfValid(ChessBoard board,List<int[]> moves, int r, int c){
        if(!inBounds(r,c)) return false;
        Piece t= board.getPiece(r,c);
        if(t == null){
            moves.add(new int[]{r,c});
            return true;
        }
        if(t.color != this.color){
            moves.add(new int[]{r,c});
        }
        return false;
    }
    protected void addSlidingMoves(ChessBoard board ,List<int[]> moves,int[][] dirs){
        for(int[] d: dirs){
            int r=row+d[0],c=col+d[1];
            while(inBounds(r,c)){
                Piece t= board.getPiece(r,c);
                if(t == null){
                    moves.add(new int[]{r,c});
                }
                else  if(t.color != this.color){
                    moves.add(new int[]{r,c});
                    break;
                }
                else {
                    break;
                }
                r=r+d[0];
                c=c+d[1];
            }
        }
    }
    public Color getColor() {
        return color;
    }
    public  Type getType() {
        return type;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public boolean hasMoved() {
        return hasMoved;
    }
    public void setRow(int r) {
        this.row = r;
    }
    public void setCol(int c) {
        this.col = c;
    }
    public void setMoved() {
        this.hasMoved = true;
    }

    public abstract Piece copy();
}

