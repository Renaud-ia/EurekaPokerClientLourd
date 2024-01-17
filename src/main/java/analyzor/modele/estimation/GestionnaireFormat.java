package analyzor.modele.estimation;

import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.ErreurBDD;
import analyzor.modele.parties.*;
import analyzor.modele.bdd.ConnexionBDD;
import jakarta.persistence.criteria.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// todo : revoir cette classe qui est juste horrible
public class GestionnaireFormat {
    private static Logger logger = LogManager.getLogger(GestionnaireFormat.class);
    private static Session session;

    // méthodes de l'interface de gestion des formats

    /** labellise les parties et renvoie le nombre de parties
     /*  renvoie null si création pas possible
     /*  accepte les doublons (many to many)
     /*  attention doublon critères avec getEntrees
     */
    public static FormatSolution ajouterFormat(FormatSolution formatSolution) {
        session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();
        try {
            List<Variante> variantes =
                    selectionnerVariantes(formatSolution.getNomFormat(),
                            formatSolution.getAnte(), formatSolution.getKO());

            List<Partie> parties =
                    selectionnerParties(variantes, formatSolution.getNombreJoueurs(),
                            formatSolution.getMinBuyIn(), formatSolution.getMaxBuyIn());

            formatSolution.getParties().addAll(parties);
            int nParties = parties.size();
            formatSolution.setNumberOfParties(nParties);

            session.persist(formatSolution);
            transaction.commit();
            ConnexionBDD.fermerSession(session);
            return formatSolution;
        }
        catch (Exception e) {
            logger.error("Pas réussi à enregistrer format Solution", e);
            transaction.rollback();
            ConnexionBDD.fermerSession(session);
            return null;
        }
    }

    public static List<FormatSolution> formatsDisponibles() {

        Session session = ConnexionBDD.ouvrirSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<FormatSolution> query = cb.createQuery(FormatSolution.class);
        Root<FormatSolution> formatSolutionRoot = query.from(FormatSolution.class);
        query.select(formatSolutionRoot);

        List<FormatSolution> formatsDispo = session.createQuery(query).getResultList();
        ConnexionBDD.fermerSession(session);

        return formatsDispo;
    }

    // appelé après import de mains, vérifie pour les nouvelles parties vides de formatSolution s'il y a correspondance
    public static void actualiserFormats() {

    }

    // supprimer tous les labels des parties
    public static void supprimerFormat(long idBDD) {
        Session session = ConnexionBDD.ouvrirSession();

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
        ConnexionBDD.fermerSession(session);
    }

    public static FormatSolution getFormatSolution(Long idBDD) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> criteria = builder.createQuery(FormatSolution.class);
        Root<FormatSolution> root = criteria.from(FormatSolution.class);

        criteria.where(builder.equal(root.get("id"), idBDD));

        FormatSolution entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            throw new ErreurBDD("Format solution non trouvé dans BBD");
        }

        ConnexionBDD.fermerSession(session);

        return entite;
    }

    // méthode pour récupérer des entrées
    //todo : est un peu lent

    /**
     * procédure pour obtenir les entrées
     * récupère les entrées correspondantes aux parties
     * attention doublon critères avec ajouterFormat
     */
    public static List<Entree> getEntrees(FormatSolution formatSolution,
                                          List<NoeudAbstrait> situationsGroupees,
                                          ProfilJoueur profilJoueur) {
        session = ConnexionBDD.ouvrirSession();
        List<Long> idNoeudCherches = getIdNoeuds(situationsGroupees);

        List<Variante> variantes =
                selectionnerVariantes(formatSolution.getNomFormat(),
                        formatSolution.getAnte(), formatSolution.getKO());

        List<Partie> parties =
                selectionnerParties(variantes, formatSolution.getNombreJoueurs(),
                        formatSolution.getMinBuyIn(), formatSolution.getMaxBuyIn());

        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Entree> entreeCriteria = builder.createQuery(Entree.class);
        Root<Entree> entreeRoot = entreeCriteria.from(Entree.class);

        // il faut charger joueur en JoinType car on va avoir besoin
        Join<Entree, Joueur> joueurJoin = entreeRoot.join("joueur");
        entreeRoot.fetch("joueur", JoinType.INNER);
        Join<Entree, TourMain> tourMainJoin = entreeRoot.join("tourMain");
        Join<TourMain, MainEnregistree> mainJoin = tourMainJoin.join("main");

        if (Objects.equals(profilJoueur.getNom(), ProfilJoueur.nomProfilHero)) {
            entreeRoot.fetch("tourMain", JoinType.INNER);
            // main sera chargé car EAGER
        }

        Predicate isMemberPredicate = builder.isMember(profilJoueur, joueurJoin.get("profils"));

        entreeCriteria.select(entreeRoot).where(
                builder.isTrue(entreeRoot.get("idNoeudTheorique").in(idNoeudCherches)),
                builder.isTrue(mainJoin.get("partie").in(parties)),
                isMemberPredicate
        );

        List<Entree> listEntrees = session.createQuery(entreeCriteria).getResultList();

        //on ferme la session il faudra remerger les objets si on a besoin de les modifier
        ConnexionBDD.fermerSession(session);

        logger.debug("ENTREES DEMANDEES : " + listEntrees.size());

        return listEntrees;
    }


    // méthodes privées

    private static List<Variante> selectionnerVariantes(Variante.PokerFormat pokerFormat,
                                                        boolean ante, boolean ko) {
        // Créez une requête pour la classe Entree
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Variante> varianteCriteria = builder.createQuery(Variante.class);
        Root<Variante> varianteRoot = varianteCriteria.from(Variante.class);

        float valueAnte;
        // petit trick si pas d'ante, vaudra 0
        // laisse la possibilité de fixer des Ante
        if (ante) valueAnte = 0.001f;
        else valueAnte = 300f;

        varianteCriteria.select(varianteRoot).where(
                builder.equal(varianteRoot.get("format"), pokerFormat),
                builder.lessThan(varianteRoot.get("ante"), valueAnte),
                builder.equal(varianteRoot.get("ko"), ko)
        );

        return session.createQuery(varianteCriteria).getResultList();
    }

    private static List<Partie> selectionnerParties(List<Variante> variantes, int nombreJoueurs,
                                                    float minBuyIn, float maxBuyIn) {
        // Créez une requête pour la classe Entree
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Partie> partieCriteria = builder.createQuery(Partie.class);
        Root<Partie> partieRoot = partieCriteria.from(Partie.class);

        partieCriteria.select(partieRoot).where(
                builder.isTrue(partieRoot.get("variante").in(variantes)),
                builder.equal(partieRoot.get("nPlayers"), nombreJoueurs),
                builder.greaterThanOrEqualTo(partieRoot.get("buyIn"), minBuyIn),
                builder.lessThanOrEqualTo(partieRoot.get("buyIn"), maxBuyIn)
        );

        return session.createQuery(partieCriteria).getResultList();
    }

    private static List<Long> getIdNoeuds(List<NoeudAbstrait> situationsGroupees) {
        List<Long> idNoeuds = new ArrayList<>();
        for (NoeudAbstrait noeudCherche : situationsGroupees) {
            idNoeuds.add(noeudCherche.toLong());
        }

        return idNoeuds;
    }
}
