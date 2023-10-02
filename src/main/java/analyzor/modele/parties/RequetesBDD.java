package analyzor.modele.parties;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class RequetesBDD {
    /*
        deprecated
    public static Situation getOrCreateSituation(int rang, int nJoueursActifs, int tour, int position) {

        Session session = HibernateUtil.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaQuery<Situation> query = criteriaBuilder.createQuery(Situation.class);
        Root<Situation> root = query.from(Situation.class);

        query.where(
                criteriaBuilder.equal(root.get("rang"), rang),
                criteriaBuilder.equal(root.get("nJoueursActifs"), nJoueursActifs),
                criteriaBuilder.equal(root.get("tour"), tour),
                criteriaBuilder.equal(root.get("position"), position)
        );

        Situation situation = session.createQuery(query).uniqueResult();

        if (situation == null) {
            System.out.println("On va créer la situation");
            situation = new Situation(rang, nJoueursActifs, tour, position);

            Transaction transaction = session.beginTransaction();
            session.persist(situation);
            transaction.commit();
        }

        session.close();
        return situation;
    }
     */

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
                // Si vous excluez le champ "id" et que le champ actuel est "id", passez au champ suivant
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

        // Créez une requête avec les prédicats non null
        query.where(criteriaBuilder.and(predicates));

        Object result = session.createQuery(query).uniqueResult();

        if (result == null) {
            // Créez un nouvel objet si aucun résultat n'a été trouvé
            Transaction transaction = session.beginTransaction();
            session.persist(objet);
            transaction.commit();

            return objet;
        }

        session.close();

        return result;
    }
}
