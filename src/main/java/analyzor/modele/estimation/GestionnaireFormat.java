package analyzor.modele.estimation;

import analyzor.modele.parties.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class GestionnaireFormat {
    public static List<FormatSolution> formatsDisponibles() {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<FormatSolution> query = cb.createQuery(FormatSolution.class);
        Root<FormatSolution> formatSolutionRoot = query.from(FormatSolution.class);
        query.select(formatSolutionRoot);

        List<FormatSolution> formatsDispo = session.createQuery(query).getResultList();
        RequetesBDD.fermerSession();

        return formatsDispo;
    }

    /** labellise les parties et renvoie le nombre de parties
    /*  renvoie null si création pas possible
    /*  accepte les doublons (many to many)
    /*  attention doublon critères avec getEntrees
     */
    public static FormatSolution ajouterFormat(FormatSolution formatSolution) {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        Transaction transaction = session.beginTransaction();
        try {
            // Créez une requête pour la classe Entree
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Partie> entreeCriteria = builder.createQuery(Partie.class);
            Root<Variante> varianteRoot = entreeCriteria.from(Variante.class);
            Join<Variante, Partie> partieJoin = varianteRoot.join("parties");

            float valueAnte;
            // petit trick si pas d'ante, vaudra 0
            // laisse la possibilité de fixer des Ante
            if (!formatSolution.getAnte()) valueAnte = 0.001f;
            else valueAnte = 300f;

            entreeCriteria.select(partieJoin).where(
                    builder.equal(varianteRoot.get("format"), formatSolution.getNomFormat()),
                    builder.lessThan(varianteRoot.get("ante"), valueAnte),
                    builder.equal(varianteRoot.get("ko"), formatSolution.getKO()),
                    builder.equal(varianteRoot.get("nPlayers"), formatSolution.getNombreJoueurs()),
                    builder.greaterThanOrEqualTo(partieJoin.get("buyIn"), formatSolution.getMinBuyIn()),
                    builder.lessThanOrEqualTo(partieJoin.get("buyIn"), formatSolution.getMaxBuyIn())
            );

            List<Partie> listParties = session.createQuery(entreeCriteria).getResultList();
            formatSolution.getParties().addAll(listParties);
            int nParties = listParties.size();
            formatSolution.setNumberOfParties(nParties);

            session.persist(formatSolution);
            transaction.commit();
            RequetesBDD.fermerSession();
            return formatSolution;
        }
        catch (Exception e) {
            transaction.rollback();
            RequetesBDD.fermerSession();
            return null;
        }
    }

    /**
     * procédure pour obtenir les entrées
     * récupère les entrées correspondantes aux parties
     * attention doublon critères avec ajouterFormat
     */
    public static List<Entree> getEntrees(FormatSolution formatSolution, TourMain.Round round) {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Entree> entreeCriteria = builder.createQuery(Entree.class);
        Root<Variante> varianteRoot = entreeCriteria.from(Variante.class);
        Join<Variante, Partie> partieJoin = varianteRoot.join("parties");
        Join<Partie, MainEnregistree> mainJoin = partieJoin.join("mainsEnregistrees");
        Join<MainEnregistree, TourMain> tourJoin = mainJoin.join("toursMain");
        Join<TourMain, Entree> entreeJoin = tourJoin.join("entrees");

        float valueAnte;
        // petit trick si pas d'ante, vaudra 0
        // laisse la possibilité de fixer des Ante
        if (!formatSolution.getAnte()) valueAnte = 0.001f;
        else valueAnte = 300f;

        entreeCriteria.select(entreeJoin).where(
                builder.equal(varianteRoot.get("format"), formatSolution.getNomFormat()),
                builder.lessThan(varianteRoot.get("ante"), valueAnte),
                builder.equal(varianteRoot.get("ko"), formatSolution.getKO()),
                builder.equal(varianteRoot.get("nPlayers"), formatSolution.getNombreJoueurs()),
                builder.greaterThanOrEqualTo(partieJoin.get("buyIn"), formatSolution.getMinBuyIn()),
                builder.lessThanOrEqualTo(partieJoin.get("buyIn"), formatSolution.getMaxBuyIn()),
                builder.equal(tourJoin.get("nomTour"), round)
        );

        List<Entree> listEntrees = session.createQuery(entreeCriteria).getResultList();

        //on ferme la session il faudra remerger les objets si on a besoin de les modifier
        RequetesBDD.fermerSession();

        return listEntrees;
    }

    // appelé après import de mains, vérifie pour les nouvelles parties vides de formatSolution s'il y a correspondance
    public static void actualiserFormats() {

    }

    // supprimer tous les labels des parties
    public static void supprimerFormat(long idBDD) {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        Transaction tx = session.beginTransaction();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> criteria = builder.createQuery(FormatSolution.class);
        Root<FormatSolution> root = criteria.from(FormatSolution.class);

        criteria.where(builder.equal(root.get("id"), idBDD));

        FormatSolution entite = session.createQuery(criteria).uniqueResult();

        if (entite != null) {
            session.remove(entite);
        }

        tx.commit();
        RequetesBDD.fermerSession();
    }

}
