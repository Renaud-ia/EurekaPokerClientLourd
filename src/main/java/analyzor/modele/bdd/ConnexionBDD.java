package analyzor.modele.bdd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ConnexionBDD {
    private final static List<Session> sessionOuvertes = new ArrayList<>();
    public static Session ouvrirSession() {
        Session session = HibernateUtil.getSession();

        sessionOuvertes.add(session);

        return session;
    }

    public static void fermerSession(Session session) {
        if (!session.isOpen()) {
        }
        else session.close();

        sessionOuvertes.remove(session);

    }

    private static class HibernateUtil {
        private static final SessionFactory sessionFactory;

        static {
            try {
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
                sessionFactory = configuration.buildSessionFactory();
            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        public static Session getSession() {
            return sessionFactory.openSession();
        }
    }

}
