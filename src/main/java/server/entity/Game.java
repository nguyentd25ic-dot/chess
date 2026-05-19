package server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "player_white", length = 50)
    private String playerWhite;

    @Column(name = "player_black", length = 50)
    private String playerBlack;

    @Column(length = 10)
    private String winner;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String moves;

    @Column(name = "total_moves")
    private int totalMoves;

    @Column(name = "played_at")
    private LocalDateTime playedAt = LocalDateTime.now();

    public Game() {}
    public Game(String white, String black, String winner, String moves) {
        this.playerWhite = white;
        this.playerBlack = black;
        this.winner      = winner;
        this.moves       = moves;
        this.totalMoves  = moves.isBlank() ? 0 : moves.split(" ").length;
    }

    public int getId(){
        return id;
    }
    public String getPlayerWhite(){
        return playerWhite;
    }
    public String getPlayerBlack(){
        return playerBlack;
    }
    public String getWinner(){
        return winner;
    }
    public String getMoves(){
        return moves;
    }
    public int getTotalMoves(){
        return totalMoves;
    }
    public LocalDateTime getPlayedAt(){
        return playedAt;
    }
}