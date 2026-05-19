package common.pieces;
import common.Piece;
import common.ChessBoard;
import java.util.*;
class King extends Piece {
    public King(Color color, int r, int c) { super(color, Type.KING, r, c); }

    @Override
    public List<int[]> getPseudoMoves(ChessBoard board) {
        List<int[]> moves = new ArrayList<>();
        for (int[] d : new int[][]{{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}})
            addIfValid(board, moves, row+d[0], col+d[1]);
            //nhap thanh king side
        if (!hasMoved && !board.isIncheck(color)) {
            Piece kr = board.getPiece(row, 7);
            if (kr != null && !kr.hasMoved()
                    && board.getPiece(row,5)==null && board.getPiece(row,6)==null) {
                ChessBoard c = board.copy(); c.movePiece(row,col,row,5);
                if (!c.isIncheck(color)) moves.add(new int[]{row,6,1});
            }
            // Nhập thành queenside
            Piece qr = board.getPiece(row, 0);
            if (qr != null && !qr.hasMoved()
                    && board.getPiece(row,1)==null && board.getPiece(row,2)==null
                    && board.getPiece(row,3)==null) {
                ChessBoard c = board.copy(); c.movePiece(row,col,row,3);
                if (!c.isIncheck(color)) moves.add(new int[]{row,2,2});
            }
        }
        return moves;
    }

    @Override public Piece copy() {
        King k = new King(color, row, col); if(hasMoved) k.setMoved(); return k;
    }
}
class Queen extends Piece {
    public Queen(Color color, int r, int c) {
        super(color,Type.QUEEN, r, c);
    }
    @Override
    public List<int[]> getPseudoMoves(ChessBoard board){
        List<int[]> moves = new ArrayList<>();
        addSlidingMoves(board, moves, new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}});
        return moves;
    }
    @Override public Piece copy() {
        Queen q = new Queen(color, row, col);
        if(hasMoved) q.setMoved();
        return q;
    }
}
class Rook extends Piece {
    public Rook(Color color, int r, int c) {
        super(color,Type.ROOK, r, c);
    }
    @Override
    public List<int[]> getPseudoMoves (ChessBoard board){
        List<int[]> moves = new ArrayList<>();
        addSlidingMoves(board, moves, new int[][]{{-1,0},{1,0},{0,-1},{0,1}});
        return moves;
    }
    @Override public Piece copy() {
        Rook r = new Rook(color, row, col);
        if(hasMoved) r.setMoved();
        return r;
    }
}
class Knight extends Piece {
    public Knight(Color color, int r, int c) {
        super(color,Type.KNIGHT, r, c);
    }
    @Override
    public List<int[]> getPseudoMoves(ChessBoard board){
        List<int[]> moves = new ArrayList<>();
        for (int[] j: new int [][]{{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}})
            addIfValid(board, moves,row+j[0],col+j[1]);
        return moves;
    }
    @Override public Piece copy() {
        Knight k = new Knight(color, row, col);
        if(hasMoved) k.setMoved();
        return k;
    }
}
class Bishop extends Piece {
    public Bishop(Color color, int r, int c) {
        super(color,Type.BISHOP, r, c);
    }
    @Override
    public List<int[]> getPseudoMoves(ChessBoard board){
        List<int[]> moves = new ArrayList<>();
        addSlidingMoves(board,moves,new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}});
        return moves;
    }
    @Override public Piece copy() {
        Bishop b = new Bishop(color, row, col);
        if(hasMoved) b.setMoved();
        return b;
    }
}
class Pawn extends Piece {
    public Pawn(Color color, int r, int c) {
        super(color,Type.PAWN, r, c);
    }
    @Override
    public List<int[]> getPseudoMoves (ChessBoard board){
        List<int[]> moves = new ArrayList<>();
        int dir = (color == Color.WHITE) ? -1 : 1;
        int startRow=(color == Color.WHITE) ? 6:1;
        if (inBounds(row+dir, col)&&board.getPiece(row+dir,col)==null) {
            moves.add(new int[]{row+dir,col});
            if (row==startRow&&board.getPiece(row+2*dir,col)==null)
                moves.add(new int[]{row+2*dir,col});
        }
        for (int dc: new int[]{-1,1}){
            if(!inBounds(row+dir, col+dc)) continue;
            Piece t= board.getPiece(row+dir,col+dc);
            if(t!=null&&t.getColor()!=color){
                moves.add(new int[]{row+dir,col+dc});
            }
            //en passant
            if (board.getEnPassantCol()==col+dc&& board.getEnPassantRow()==row+dir)
                moves.add(new int[]{row+dir,col+dc,3});
        }
        return moves;
    }
    @Override public Piece copy() {
        Pawn p = new Pawn(color, row, col);
        if(hasMoved) p.setMoved();
        return p;
    }
}
public class PieceFactory {
    public static Piece createPiece(Piece.Type type,Piece.Color color, int r, int c) {
        return switch(type){
            case KING -> new King(color, r, c);
            case QUEEN -> new Queen(color, r, c);
            case ROOK -> new Rook(color, r, c);
            case BISHOP -> new Bishop(color, r, c);
            case PAWN -> new Pawn(color, r, c);
            case KNIGHT -> new Knight(color, r, c);
        };
    }
}
