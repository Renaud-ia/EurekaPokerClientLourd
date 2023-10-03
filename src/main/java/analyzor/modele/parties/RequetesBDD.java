package analyzor.modele.parties;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class RequetesBDD {
    public static Object getOrCreate(Object objet, boolean id_field) {
        /*
        si il y a un champ id, il faut qu'il soit nommé "id"
         */
        Session session = HibernateUtil.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        Class<?> classe = objet.getClass();

        CriteriaQuery<?> query = criteriaBuilder.createQuery(classe);
        Root<?> root = query.from(classe);

        Predicate[] predicates;
        if (id_field) predicates = new Predicate[classe.getDeclaredFields().length - 1];
        else predicates = new Predicate[classe.getDeclaredFields().length];

        int i = 0;
        for (java.lang.reflect.Field field : classe.getDeclaredFields()) {
            field.setAccessible(true); // Permet d'accéder aux champs privés

            String nameField = field.getName();
            if (id_field && nameField.equals("id")) {
                continue;
            }

            try {
                Object value = field.get(objet);
                if (value != null && !nameField.equals("id")) {
                    predicates[i] = criteriaBuilder.equal(root.get(nameField), value.toString());
                    i++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
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

        // si l'objet n'existe pas
        if (objetResultant == null) {
            Transaction transaction = session.beginTransaction();
            session.persist(objet);
            transaction.commit();

            objetResultant = objet;
        }

        session.close();
        return objetResultant;
    }
}
