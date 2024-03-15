package analyzor.modele.bdd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ConnexionBDD {
    private static final Logger logger = LogManager.getLogger(ConnexionBDD.class);
    private final static List<Session> sessionOuvertes = new ArrayList<>();
    public static Session ouvrirSession() {
        Session session = HibernateUtil.getSession();

        sessionOuvertes.add(session);

        logger.trace("Nouvelle session ouverte");
        logger.trace("Nombre de session ouvertes : " + sessionOuvertes.size());
        return session;
    }

    public static void fermerSession(Session session) {
        if (!session.isOpen()) {
            logger.warn("La session n'est pas ouverte");
        }
        else session.close();

        if (sessionOuvertes.contains(session)) {
            sessionOuvertes.remove(session);
        }
        else {
            logger.warn("La session n'a pas été trouvée");
        }

    }

    private static class HibernateUtil {
        private static final SessionFactory sessionFactory;

        static {
            try {
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
                sessionFactory = configuration.buildSessionFactory();
            } catch (Throwable ex) {
                logger.fatal("Impossible de configurer la connexion à la BDD", ex);
                throw new ExceptionInInitializerError(ex);
            }
            logger.trace("Configuration BDD OK");
        }

        public static Session getSession() {
            return sessionFactory.openSession();
        }
    }

}
