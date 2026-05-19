package server.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="players")
public class Player {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(unique = true,nullable = false,length = 50)
    private String username;

    private int wins=0;
    private int losses=0;
    private int draws=0;

    @Column(name="created_at")
    private LocalDateTime createdAt=LocalDateTime.now();

    public Player() {}
    public Player(String username){
        this.username = username;
    }
    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public int  getWins() {
        return wins;
    }
    public int getLosses() {
        return losses;
    }
    public int getDraws() {
        return draws;
    }
    public void addWin(){
        wins++;
    }
    public void addLoss(){
        losses++;
    }
    public void addDraw(){
        draws++;
    }
}
