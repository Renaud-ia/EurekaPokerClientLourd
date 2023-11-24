package analyzor.modele.arbre;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Joueur;
import analyzor.modele.parties.RequetesBDD;
import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.RangeSauvegardable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * centralise les aspects de récupération de données dans la BDD
 * attention : récupère la range qui précéde l'entrée donnée !
 */
public class RecuperateurRange {
    // valeurs utilisées pour choisir la range la plus proche
    // todo tester les bonnes valeurs
    private static float POIDS_SPR = 0.8f;
    private static float POIDS_POT = 0.8f;
    private static float POIDS_POT_BOUNTY = 0.8f;
    private static float POIDS_BET_SIZE = 0.5f;
    Session session;
    Transaction transaction;
    private FormatSolution formatSolution;
    public RecuperateurRange(FormatSolution formatSolution) {
        this.formatSolution = formatSolution;
    }

    /**
     * on parcourt les entrees suivantes et on récupère les villains qui vont jouer
     * la session doit avoir été ouverte
      */
    List<Joueur> trouverVillainsActifs(Entree entree) {
        TourMain tourMain = entree.getTourMain();
        int indexAction = entree.getIdAction();
        Joueur hero = entree.getJoueur();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Joueur> query = cb.createQuery(Joueur.class);
        Root<Entree> entreeRoot = query.from(Entree.class);
        Join<Entree, Joueur> joueurJoin = entreeRoot.join("joueur");

        query.select(joueurJoin).where(
                cb.equal(entreeRoot.get("tourMain"), tourMain),
                cb.greaterThan(entreeRoot.get("numAction"), indexAction),
                cb.notEqual(entreeRoot.get("joueur"), hero)
        );


        List<Joueur> villains = session.createQuery(query).getResultList();

        return villains;
    }

    /**
     * on récupère les entrées précédentes
     * la session doit avoir été ouverte
     */
    List<Entree> recupererEntreesPrecedentes(Entree entree) {
        TourMain tourMain = entree.getTourMain();
        int indexAction = entree.getIdAction();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Entree> query = cb.createQuery(Entree.class);
        Root<Entree> entreeRoot = query.from(Entree.class);

        query.select(entreeRoot).where(
                cb.equal(entreeRoot.get("tourMain"), tourMain),
                cb.lessThan(entreeRoot.get("numAction"), indexAction)
        );

        List<Entree> entreesPrecedentes = session.createQuery(query).getResultList();

        return entreesPrecedentes;
    }

    /**
     * procédure centralisée de récupération des ranges relatives sauvegardées
     * définit des critères normalisés sur SPRB/BetSize etc.
     * gère automatiquement la fermeture ouverture de session
     * récupère le noeud le plus proche
     */
    public RangeSauvegardable selectionnerRange(long idNoeudTheorique, float stackEffectif, float pot,
                                                float potBounty, float betSize) {
        boolean sessionDejaOuverte = (session != null);
        if (!sessionDejaOuverte) this.ouvrirSession();

        List<NoeudAction> noeudsCorrespondants = trouverNoeudActionBDD(idNoeudTheorique);

        float distanceMax = Float.MAX_VALUE;
        NoeudAction noeudPlusProche = null;

        for (NoeudAction noeudTrouve : noeudsCorrespondants) {
            float distance = 0;
            distance += Math.abs(noeudTrouve.getStackEffectif() - stackEffectif) * POIDS_SPR;
            distance += Math.abs(noeudTrouve.getPot() - pot) * POIDS_POT;
            distance += Math.abs(noeudTrouve.getPotBounty() - potBounty) * POIDS_POT_BOUNTY;
            distance += Math.abs(noeudTrouve.getBetSize() - betSize) * POIDS_BET_SIZE;

            if (distance < distanceMax) {
                distanceMax = distance;
                noeudPlusProche = noeudTrouve;
            }
        }

        // on prend la range qui correspond au noeud (une seule normalement)
        RangeSauvegardable rangeTrouvee = rangeFromNoeud(noeudPlusProche);

        if (!sessionDejaOuverte) this.fermerSession();
        return rangeTrouvee;
    }

    /**
     * on va sélectionner le noeud le plus proche
     */
    private List<NoeudAction> trouverNoeudActionBDD(long idNoeudTheorique) {
        CriteriaBuilder cbNoeud = session.getCriteriaBuilder();
        CriteriaQuery<NoeudAction> queryNoeud = cbNoeud.createQuery(NoeudAction.class);
        Root<NoeudAction> noeudActionRoot = queryNoeud.from(NoeudAction.class);

        queryNoeud.select(noeudActionRoot).where(
                cbNoeud.equal(noeudActionRoot.get("idNoeudTheorique"), idNoeudTheorique),
                cbNoeud.equal(noeudActionRoot.get("formatSolution"), this.formatSolution)
        );

        List<NoeudAction> noeudsCorrespondants = session.createQuery(queryNoeud).getResultList();
        if (!(noeudsCorrespondants.isEmpty())) return noeudsCorrespondants;

        // si on trouve pas directement le noeud cherché on va faire appel à l'arbre
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        List<NoeudAbstrait> noeudsPlusProches = arbreAbstrait.noeudsPlusProches(noeudAbstrait);
        int index = 0;
        while (noeudsCorrespondants.isEmpty()) {
            if (index > noeudsPlusProches.size())
                throw new RuntimeException("Aucune range trouvée pour : " + noeudAbstrait);

            NoeudAbstrait noeudTeste = noeudsPlusProches.get(index++);
            queryNoeud.select(noeudActionRoot).where(
                    cbNoeud.equal(noeudActionRoot.get("idNoeudTheorique"), noeudTeste.toLong()),
                    cbNoeud.equal(noeudActionRoot.get("formatSolution"), this.formatSolution)
            );
            noeudsCorrespondants = session.createQuery(queryNoeud).getResultList();
        }

        return noeudsCorrespondants;
    }

    private RangeSauvegardable rangeFromNoeud(NoeudAction noeudAction) {
        CriteriaBuilder cbRange = session.getCriteriaBuilder();
        CriteriaQuery<RangeSauvegardable> queryRange = cbRange.createQuery(RangeSauvegardable.class);
        Root<RangeSauvegardable> rangeRoot = queryRange.from(RangeSauvegardable.class);

        queryRange.select(rangeRoot).where(
                cbRange.equal(rangeRoot.get("noeudArbre"), noeudAction)
        );

        return session.createQuery(queryRange).uniqueResult();
    }

    protected void ouvrirSession() {
        RequetesBDD.ouvrirSession();
        session = RequetesBDD.getSession();
    }

    protected void fermerSession() {
        RequetesBDD.fermerSession();
        session = null;
    }
}
