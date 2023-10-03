package analyzor.modele.parties;

import analyzor.modele.exceptions.ErreurInterne;
import analyzor.modele.logging.GestionnaireLog;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class RequetesBDD {
    private static final Logger logger = GestionnaireLog.getLogger("Requetes BDD");
    static {
        GestionnaireLog.setHandler(logger, GestionnaireLog.warningBDD);
        GestionnaireLog.setHandler(logger, GestionnaireLog.debugBDD);
    }
    private static Session session;
    public static Object getOrCreate(Object objet, boolean id_field) throws ErreurInterne {
        /*
        IMPORTANT
        si il y a un champ id, il faut qu'il soit nommé "id"
        si un attribut n'est pas initialisé, ce doit être un objet qui doit valoir null
         */
        if (!ouvrirSession()) return null;
        Session session = getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        Class<?> classe = objet.getClass();

        CriteriaQuery<?> query = criteriaBuilder.createQuery(classe);
        Root<?> root = query.from(classe);

        long valeursNonNulles = Arrays.stream(classe.getDeclaredFields())
                .filter(field -> {
                    try {
                        field.setAccessible(true); // Permet d'accéder aux champs privés
                        return field.get(objet) != null && !(field.get(objet) instanceof List);
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                })
                .count();

        logger.finest("Valeurs non nulles :" + valeursNonNulles);

        Predicate[] predicates;
        if (id_field) predicates = new Predicate[(int) valeursNonNulles - 1];
        else predicates = new Predicate[(int) valeursNonNulles];

        int i = 0;
        for (java.lang.reflect.Field field : classe.getDeclaredFields()) {
            logger.finest("On loop sur un field");
            try {
            field.setAccessible(true); // Permet d'accéder aux champs privés
            if (field.get(objet) == null || field.get(objet) instanceof List) {
                logger.fine("La valeur suivante sera ignorée : " + field);
                continue;
            }

            String nameField = field.getName();
            if (id_field && nameField.equals("id")) {
                continue;
            }

                Object value = field.get(objet);
                if (value != null && !nameField.equals("id")) {
                    predicates[i] = criteriaBuilder.equal(root.get(nameField), value.toString());
                    i++;
                    logger.fine("Valeur ajoutée dans les critères de recherche : " + root.get(nameField) + ", "+ value.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new ErreurInterne("Impossible d'accéder aux champs privés de la classe");
            }
        }

        query.where(criteriaBuilder.and(predicates));

        List<?> results = session.createQuery(query).getResultList();

        int compte = 0;
        Object objetResultant = null;
        for (Object objetTrouve: results) {
            // on ne veut pas les objets d'instance différente (= héritage)
            if (!objetTrouve.getClass().equals(classe)) {
                continue;
            }
            compte++;
            objetResultant = objetTrouve;
        }

        // plus d'un objet => on lève une erreur
        if (compte > 1) throw new NonUniqueResultException(compte);
        if (compte == 0) System.out.println("Aucune objet trouvé");

        // si l'objet n'existe pas
        if (objetResultant == null) {
            Transaction transaction = session.beginTransaction();
            session.persist(objet);
            transaction.commit();

            objetResultant = objet;
        }

        fermerSession();
        return objetResultant;
    }
    public static boolean ouvrirSession() {
        if (session != null && session.isOpen()) {
            logger.warning("Session déjà ouverte");
            return false;
        }
        else {
            session = HibernateUtil.getSession();
            logger.finer("Session BBD ouverte");
            return true;
        }
    }

    public static Session getSession() {
        return session;
    }

    public static void fermerSession() {
        logger.fine("Session fermée : " + session.isOpen());
        session.close();
    }

    private static class HibernateUtil {
        private static final SessionFactory sessionFactory;

        static {
            try {
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
                sessionFactory = configuration.buildSessionFactory();
            } catch (Throwable ex) {
                logger.info("Impossible de configurer la connexion à la BDD");
                throw new ExceptionInInitializerError(ex);
            }
            logger.finest("Configuration BDD OK");
        }

        public static Session getSession() {
            return sessionFactory.openSession();
        }
    }

}
