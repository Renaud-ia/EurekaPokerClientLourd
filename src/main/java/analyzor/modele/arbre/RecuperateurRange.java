package analyzor.modele.arbre;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.*;
import analyzor.modele.utils.RequetesBDD;
import analyzor.modele.poker.RangeSauvegardable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * centralise les aspects de récupération de données dans la BDD
 * attention : récupère la range qui précéde l'entrée donnée !
 */
public class RecuperateurRange {
    protected final Logger logger = LogManager.getLogger(RecuperateurRange.class);
    // valeurs utilisées pour choisir la range la plus proche
    // todo tester les bonnes valeurs
    private final static float POIDS_SPR = 0.8f;
    private final static float POIDS_POT = 0.8f;
    private final static float POIDS_POT_BOUNTY = 0.8f;
    private final static float POIDS_BET_SIZE = 0.5f;
    Session session;
    private final FormatSolution formatSolution;
    public RecuperateurRange(FormatSolution formatSolution) {
        // on a besoin du format de la Solution pour explorer les noeuds plus proches
        this.formatSolution = formatSolution;
    }

    /**
     * retourne dans l'ordre des actions les villains actifs pour une entrée donnée
     * on parcourt les entrees suivantes et on récupère les villains qui sont actifs
     * c'est à dire qui ont joué dans le tour sans fold
     * si que des fold on aura 0 villains actifs
     * la session doit avoir été ouverte
      */
    List<Joueur> trouverVillainsActifs(Entree entree) {
        TourMain tourMain = entree.getTourMain();
        int indexAction = entree.getIdAction();
        Joueur hero = entree.getJoueur();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Entree> query = cb.createQuery(Entree.class);
        Root<Entree> entreeRoot = query.from(Entree.class);

        query.select(entreeRoot).where(
                cb.equal(entreeRoot.get("tourMain"), tourMain),
                cb.notEqual(entreeRoot.get("numAction"), indexAction),
                cb.notEqual(entreeRoot.get("joueur"), hero)
        );

        // on trie dans l'ordre des actions
        query.orderBy(cb.asc(entreeRoot.get("numAction")));


        List<Joueur> villains = new ArrayList<>();

        for (Entree entreeVillain : session.createQuery(query).getResultList()) {
            Joueur villain = entreeVillain.getJoueur();
            Long idNoeudTheorique = entreeVillain.getIdNoeudTheorique();
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
            // si le joueur a fold avant l'action de hero, on ne le compte pas
            if (noeudAbstrait.getMove() == Move.FOLD && entreeVillain.getNumAction() < indexAction) {
                villains.remove(villain);
                logger.trace("Villain retiré car FOLD : " + villain);
            }
            else {
                if (!(villains.contains(villain))) {
                    villains.add(villain);
                    logger.trace("Villain trouvé : " + villain);
                }

            }
        }

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

        // on trie dans l'ordre des actions
        query.orderBy(cb.asc(entreeRoot.get("numAction")));

        return session.createQuery(query).getResultList();
    }

    /**
     * procédure centralisée de récupération des ranges relatives sauvegardées
     * définit des critères normalisés sur SPRB/BetSize etc.
     * gère automatiquement la fermeture ouverture de session
     * récupère le noeud le plus proche
     */
    public RangeSauvegardable selectionnerRange(long idNoeudTheorique, float stackEffectif, float pot,
                                                float potBounty, float betSize, ProfilJoueur profilJoueur) {
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
        RangeSauvegardable rangeTrouvee = rangeFromNoeud(noeudPlusProche, profilJoueur);

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

    private RangeSauvegardable rangeFromNoeud(NoeudAction noeudAction, ProfilJoueur profilJoueur) {
        CriteriaBuilder cbRange = session.getCriteriaBuilder();
        CriteriaQuery<RangeSauvegardable> queryRange = cbRange.createQuery(RangeSauvegardable.class);
        Root<RangeSauvegardable> rangeRoot = queryRange.from(RangeSauvegardable.class);


        if (profilJoueur == null) {
           profilJoueur = new ProfilJoueur(ValeursConfig.nomProfilVillain);
        }
        queryRange.select(rangeRoot).where(
                cbRange.equal(rangeRoot.get("noeudArbre"), noeudAction),
                cbRange.equal(rangeRoot.get("profil"), profilJoueur)
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
