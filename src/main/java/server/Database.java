package server;

import org.hibernate.Session;
import org.hibernate.Transaction;
import server.entity.Game;
import server.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class Database {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static boolean registerOrLogin(String username) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Player p = findPlayer(s, username);
            if (p == null) {
                Transaction tx = s.beginTransaction();
                s.persist(new Player(username));
                tx.commit();
            }
            return true;
        } catch (Exception e) {
            System.err.println("[DB] registerOrLogin: " + e.getMessage());
            return false;
        }
    }
    public static void saveGame(String white, String black,
                                String winner, String moves) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();

            s.persist(new Game(white, black, winner, moves));

            Player pw = findPlayer(s, white);
            Player pb = findPlayer(s, black);
            if (pw != null && pb != null) {
                switch (winner) {
                    case "white" -> { pw.addWin();  pb.addLoss(); }
                    case "black" -> { pb.addWin();  pw.addLoss(); }
                    case "draw"  -> { pw.addDraw(); pb.addDraw(); }
                }
                s.merge(pw);
                s.merge(pb);
            }

            tx.commit();
        } catch (Exception e) {
            System.err.println("[DB] saveGame: " + e.getMessage());
        }
    }
    public static List<String> getHistory(String username) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                            "FROM Game WHERE playerWhite=:u OR playerBlack=:u ORDER BY playedAt DESC",
                            Game.class)
                    .setParameter("u", username)
                    .list()
                    .stream()
                    .map(g -> g.getId()          + "|"
                            + g.getPlayerWhite() + "|"
                            + g.getPlayerBlack() + "|"
                            + g.getWinner()      + "|"
                            + g.getTotalMoves()  + "|"
                            + g.getPlayedAt().format(FMT))
                    .toList();
        } catch (Exception e) {
            System.err.println("[DB] getHistory: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static String getStats(String username) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Player p = findPlayer(s, username);
            if (p != null)
                return p.getWins() + "|" + p.getLosses() + "|" + p.getDraws();
        } catch (Exception e) {
            System.err.println("[DB] getStats: " + e.getMessage());
        }
        return "0|0|0";
    }
    private static Player findPlayer(Session s, String username) {
        return s.createQuery("FROM Player WHERE username=:u", Player.class)
                .setParameter("u", username)
                .uniqueResult();
    }
}