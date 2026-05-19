package server;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();
            System.out.println("[Hibernate] SessionFactory ready.");
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Hibernate init failed: " + e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public static void shutdown() {
        sessionFactory.close();
    }
}