package analyzor.modele.estimation;

import analyzor.modele.parties.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class GestionnaireFormat {
    public static List<FormatSolution> formatsDisponibles() {
        Session session = RequetesBDD.getSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> query = cb.createQuery(FormatSolution.class);
        List<FormatSolution> formatsDispo = session.createQuery(query).getResultList();
        session.close();

        return formatsDispo;
    }

    protected static void ajouterFormat(FormatSolution formatSolution) {
        Session session = RequetesBDD.getSession();
        Transaction transaction = session.beginTransaction();
        session.persist(formatSolution);
        transaction.commit();
        session.close();
    }

    public static int nombreParties(FormatSolution formatSolution) {
        // renvoie le nombre de parties correspondantes au format
        //todo : on pourrait avoir un GetParties dans GetEntrees
        return 0;
    }

    /**
     * procédure pour obtenir les entrées
     */
    public static List<Entree> getEntrees(FormatSolution formatSolution) {
        //Todo : remplir les critères adéquats

        // Obtenez la session Hibernate (supposons que vous l'ayez déjà)
        Session session = RequetesBDD.getSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Créez une requête pour la classe Entree
        CriteriaQuery<Entree> entreeCriteria = builder.createQuery(Entree.class);
        Root<Variante> varianteRoot = entreeCriteria.from(Variante.class);

        // Joignez la Variante aux Parties
        Join<Variante, Partie> partieJoin = varianteRoot.join("parties");

        // Joignez la Partie aux MainEnregistree
        Join<Partie, MainEnregistree> mainJoin = partieJoin.join("mainEnregistree");

        // Joignez la MainEnregistree à TourMain
        Join<MainEnregistree, TourMain> tourJoin = mainJoin.join("tourMain");

        // Joignez le TourMain à Entree
        Join<TourMain, Entree> entreeJoin = tourJoin.join("entrees");

        // Appliquez les filtres souhaités
        entreeCriteria.select(entreeJoin).where(
                builder.equal(varianteRoot.get("unAttributDeVariante"), "valeurDesirée"),
                builder.greaterThan(partieJoin.get("unAttributDePartie"), 10)
                // ... ajoutez d'autres restrictions comme nécessaire ...
        );

        // Exécutez la requête
        List<Entree> listEntrees = session.createQuery(entreeCriteria).getResultList();

        //on ferme la session il faudra remerger les objets si on a besoin de les modifier
        session.close();

        return listEntrees;
    }
}
