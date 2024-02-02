package analyzor.modele.bdd;

import analyzor.modele.exceptions.ErreurInterne;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ConnexionBDD {
    private static final Logger logger = LogManager.getLogger(ConnexionBDD.class);
    private static List<Session> sessionOuvertes = new ArrayList<>();
    private static boolean bddOuverte = true;
    public static Session ouvrirSession() {
        if (!bddOuverte) {
            System.out.println("CONNEXION REFUSEE");
            return null;
        }
        else System.out.println("CONNEXION OUVERTE");
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

    public static void statut() {
        logger.error("Nombre de sessions ouvertes : " + sessionOuvertes.size());
    }

    /**
     * @return si une connexion est ouverte à la base ou non
     */
    public static boolean connexionActive() {
        return !sessionOuvertes.isEmpty();
    }

    public static void empecherConnexion() {
        bddOuverte = false;
    }

    public static void retablirConnexion() {
        bddOuverte = true;
    }

    private static class HibernateUtil {
        private static SessionFactory sessionFactory;

        static {
            try {
                Configuration configuration = new Configuration().configure("buguepasstp.cfg.xml");
                sessionFactory = configuration.buildSessionFactory();
            } catch (Throwable ex) {
                logger.info("Impossible de configurer la connexion à la BDD", ex);
                throw new ExceptionInInitializerError(ex);
            }
            logger.trace("Configuration BDD OK");
        }

        public static Session getSession() {
            return sessionFactory.openSession();
        }
    }

}
