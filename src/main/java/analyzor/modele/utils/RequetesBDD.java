package analyzor.modele.utils;

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
import java.util.List;

public class RequetesBDD {
    private static final Logger logger = LogManager.getLogger(RequetesBDD.class);
    private static Session session;
    @Deprecated
    public static Object getOrCreate(Object objet) throws ErreurInterne {
        if (!ouvrirSession()) return null;
        Session session = getSession();
        Object objetResultant = getOrCreate(objet, session);
        fermerSession();
        return objetResultant;
    }

    @Deprecated
    public static Object getOrCreate(Object objet, Session session) throws ErreurInterne {
        /*
         IMPORTANT
         si un attribut n'est pas initialisé, ce doit être un objet qui doit valoir null
         */

        Class<?> classe = objet.getClass();

        long valeursNonNulles = 0;
        for (java.lang.reflect.Field field : classe.getDeclaredFields()) {
            field.setAccessible(true); // Permet d'accéder aux champs privés
            Annotation annotation = field.getAnnotation(Id.class);
            if (annotation != null) {
                continue;
            }
            try {
                if (field.get(objet) == null || field.get(objet) instanceof List) {
                    logger.trace("La valeur suivante sera ignorée : " + field);
                    continue;
                }
            }
            catch (IllegalAccessException e) {
                throw new ErreurInterne("Impossible d'accéder aux champs privés de la classe");
            }
            valeursNonNulles++;
        }

        logger.trace("Valeurs non nulles :" + valeursNonNulles);

        Predicate[] predicates;
        predicates = new Predicate[(int) valeursNonNulles];

        Transaction transactionQuery = session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<?> query = criteriaBuilder.createQuery(classe);
        Root<?> root = query.from(classe);

        int i = 0;
        for (java.lang.reflect.Field field : classe.getDeclaredFields()) {
            try {
            field.setAccessible(true); // Permet d'accéder aux champs privés
            if (field.get(objet) == null || field.get(objet) instanceof List) {
                logger.trace("La valeur suivante sera ignorée : " + field);
                continue;
            }

            String nameField = field.getName();
            Annotation annotation = field.getAnnotation(Id.class);
            if (annotation != null) {
                continue;
            }

                Object value = field.get(objet);
                if (value != null) {
                    predicates[i++] = criteriaBuilder.equal(root.get(nameField), value);
                    logger.trace("Valeur ajoutée dans les critères de recherche : " + root.get(nameField) + ", "+ value);
                }
            } catch (IllegalAccessException e) {
                throw new ErreurInterne("Impossible d'accéder aux champs privés de la classe");
            }
        }

        query.where(criteriaBuilder.and(predicates));
        List<?> results = session.createQuery(query).getResultList();
        transactionQuery.commit();

        int compte = 0;
        Object objetResultant = null;
        for (Object objetTrouve: results) {
            // on ne veut pas les objets d'instance différente (= héritage)
            if (!objetTrouve.getClass().equals(classe)) {
                continue;
            }
            compte++;
            objetResultant = objetTrouve;
            logger.trace("Objet trouvé");
        }

        // plus d'un objet → on lève une erreur
        if (compte > 1) {
            logger.warn("Plus d'un objet trouvé");
            throw new NonUniqueResultException(compte);
        }

        // si l'objet n'existe pas
        if (objetResultant == null) {
            logger.trace("Objet non trouvé, on le crée");
            Transaction transactionPersist = session.beginTransaction();
            session.persist(objet);
            transactionPersist.commit();
            return objet;
        }
        else {
            return objetResultant;
        }

    }
    public static boolean ouvrirSession() {
        if (session != null && session.isOpen()) {
            logger.warn("Session déjà ouverte");
            throw new RuntimeException();
        }
        else {
            session = HibernateUtil.getSession();
            logger.trace("Session BBD ouverte");
            return true;
        }
    }

    public static Session getSession() {
        return session;
    }

    public static void fermerSession() {
        session.close();
    }

    private static class HibernateUtil {
        private static final SessionFactory sessionFactory;

        static {
            try {
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
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
