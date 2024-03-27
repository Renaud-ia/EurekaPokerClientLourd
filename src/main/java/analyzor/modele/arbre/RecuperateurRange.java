package analyzor.modele.arbre;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudMesurable;
import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.berkeley.EnregistrementNormalisation;
import analyzor.modele.clustering.objets.MinMaxCalculSituation;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.ErreurCritique;
import analyzor.modele.parties.*;
import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.poker.RangeSauvegardable;
import analyzor.modele.simulation.BuilderStackEffectif;
import analyzor.modele.simulation.SituationStackPotBounty;
import analyzor.modele.simulation.StacksEffectifs;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * centralise les aspects de récupération de données dans la BDD
 * attention : récupère la range qui précéde l'entrée donnée !
 */
public class RecuperateurRange {
    protected final Logger logger = LogManager.getLogger(RecuperateurRange.class);
    Session session;
    private final FormatSolution formatSolution;
    public RecuperateurRange(FormatSolution formatSolution) {
        // on a besoin du format de la Solution pour explorer les noeuds plus proches
        this.formatSolution = formatSolution;
    }


    // interface de récupération des ranges

    /**
     * procédure centralisée de récupération des ranges relatives sauvegardées
     * définit des critères normalisés sur SPRB/BetSize etc.
     * gère automatiquement la fermeture ouverture de session
     * récupère le noeud le plus proche
     * on peut soit sélectionner le noeud Abstrait identique soit le plus proche
     * dans le premier cas, si le noeud n'existe pas return null
     */
    public RangeSauvegardable selectionnerRange(long idNoeudTheorique, long codeStackEffectif, float pot,
                                                float potBounty, float betSize, ProfilJoueur profilJoueur,
                                                boolean noeudIdentique) {
        boolean sessionDejaOuverte = (session != null);
        if (!sessionDejaOuverte) this.ouvrirSession();

        NoeudAction noeudPlusProche = noeudActionPlusProche(idNoeudTheorique, codeStackEffectif, pot, potBounty, betSize,
                profilJoueur, noeudIdentique);

        // on prend la range qui correspond au noeud (une seule normalement)
        if (noeudPlusProche == null)
            throw new RuntimeException("RR1");
        RangeSauvegardable rangeTrouvee = noeudPlusProche.getRange();

        if (!sessionDejaOuverte) this.fermerSession();
        return rangeTrouvee;
    }

    /**
     * utilisé par simulation pour récupérer les noeuds de Situation
     * @return null si aucun noeud ne correspond à l'idNoeudTheorique
     */
    public NoeudSituation noeudSituationPlusProche(long idNoeudTheorique,
                                                   SituationStackPotBounty situationStackPotBounty,
                                                   ProfilJoueur profilJoueur) {
        boolean sessionDejaOuverte = (session != null);
        if (!sessionDejaOuverte) this.ouvrirSession();

        List<NoeudSituation> noeudsCorrespondants = trouverNoeudsSituations(idNoeudTheorique, profilJoueur);
        HashMap<NoeudMesurable, Float> distances =
                distanceSituations(situationStackPotBounty, noeudsCorrespondants);

        float distanceMax = Float.MAX_VALUE;
        NoeudSituation noeudPlusProche = null;
        for (NoeudSituation noeudTrouve : noeudsCorrespondants) {
            float distance = distances.get(noeudTrouve);

            if (distance < distanceMax) {
                distanceMax = distance;
                noeudPlusProche = noeudTrouve;
            }
        }

        if (!sessionDejaOuverte) this.fermerSession();

        return noeudPlusProche;
    }

    // méthodes internes

    /**
     * renvoie le noeud action le plus proche du noeud action cherché
     * utilisé par Classificateur pour obtenir la range la plus proche de l'action précédente
     * @param idNoeudTheorique id du noeud d'action pour lequel on cherche
     * @param noeudIdentique si true, on ne cherche que le même id
     * @return le noeud action plus proche
     */
    private NoeudAction noeudActionPlusProche(long idNoeudTheorique, long codeStackEffectif, float pot,
                                        float potBounty, Float betSize, ProfilJoueur profilJoueur,
                                        boolean noeudIdentique) {

        List<NoeudAction> noeudsCorrespondants =
                trouverNoeudActionBDD(idNoeudTheorique, noeudIdentique, profilJoueur);

        if (noeudsCorrespondants.isEmpty()) {
            return null;
        }

        StacksEffectifs stacksEffectifs = BuilderStackEffectif.getStacksEffectifs(codeStackEffectif);
        SituationStackPotBounty situationStackPotBounty = new SituationStackPotBounty(
                stacksEffectifs,
                pot,
                potBounty
        );

        HashMap<NoeudMesurable, Float> distancesSituations
                = distanceSituations(situationStackPotBounty, noeudsCorrespondants);

        float distanceMax = Float.MAX_VALUE;
        NoeudAction noeudPlusProche = null;
        for (NoeudAction noeudTrouve : noeudsCorrespondants) {
            float distance = 0;
            distance += distancesSituations.get(noeudTrouve);
            // todo à changer, le betSize doit être un critère de second ordre car n'est pas comparable en terme d'échelle
            if (betSize != null) {
                distance += Math.abs(noeudTrouve.getBetSize() - betSize);
            }

            if (distance < distanceMax) {
                distanceMax = distance;
                noeudPlusProche = noeudTrouve;
            }
        }

        if (noeudPlusProche == null) return null;

        return noeudPlusProche;
    }

    /**
     * retourne dans l'ordre des actions les villains actifs pour une entrée donnée
     * on parcourt les entrees suivantes et on récupère les villains qui sont actifs
     * c'est à dire qui ont joué dans le tour sans fold
     * si que des fold on aura 0 villains actifs
     * la session doit avoir été ouverte
      */
    protected List<Joueur> trouverVillainsActifs(Entree entree) {
        TourMain tourMain = entree.getTourMain();
        int indexAction = entree.getNumAction();
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
            }
            else {
                if (!(villains.contains(villain))) {
                    villains.add(villain);
                }

            }
        }

        return villains;
    }

    /**
     * on récupère les entrées précédentes
     * la session doit avoir été ouverte
     */
    protected List<Entree> recupererEntreesPrecedentes(Entree entree) {
        TourMain tourMain = entree.getTourMain();
        int indexAction = entree.getNumAction();

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
     * renvoie la map de distances de noeud mesurables par rapport au seul critère StacksPotBounty
     * flexible : prend des noeudAction et noeudSituation
     * @param situationNoeudCherche une situation avec des données non normalisees
     * @param noeudSituations liste des noeuds qu'on veut mesurer
     * @return les distances sous forme de HashMap
     */
    private HashMap<NoeudMesurable, Float> distanceSituations(SituationStackPotBounty situationNoeudCherche,
                                                              List<? extends NoeudMesurable> noeudSituations) {
        HashMap<NoeudMesurable, Float> distances = new HashMap<>();

        EnregistrementNormalisation enregistrementNormalisation = new EnregistrementNormalisation();

        for (NoeudMesurable noeudTrouve : noeudSituations) {
            // les données enregistrement dans base de données sont déjà normalisées
            SituationStackPotBounty situationNoeudBDD = new SituationStackPotBounty(
                    BuilderStackEffectif.getStacksEffectifs(noeudTrouve.getCodeStackEffectif()),
                    noeudTrouve.getPot(),
                    noeudTrouve.getPotBounty()
            );
            // important les valeurs enregistrées sont déjà normalisées
            situationNoeudBDD.normalisationActivee(false);
            MinMaxCalculSituation minMaxCalculSituation;
            try {
                // on récupère les données de normalisation utilisés par le noeud comparé
                minMaxCalculSituation = enregistrementNormalisation.recupererMinMax(
                        noeudTrouve.getIdFormatSolution(),
                        noeudTrouve.getIdNoeudSituation()
                );
            }
            catch (Exception e) {
                throw new ErreurCritique("RR2");
            }

            // on applique la normalisation aux données cherchées
            situationNoeudCherche.activerMinMaxNormalisation(
                    minMaxCalculSituation.getMinValeurs(),
                    minMaxCalculSituation.getMaxValeurs()
            );

            float distance = situationNoeudCherche.distance(situationNoeudBDD);
            distances.put(noeudTrouve, distance);
        }

        return distances;
    }

    // todo : refactoriser cette méthode et celle qui cherche les noeuds Action
    private List<NoeudSituation> trouverNoeudsSituations(long idNoeudTheorique, ProfilJoueur profilJoueur) {
        CriteriaBuilder cbNoeud = session.getCriteriaBuilder();
        CriteriaQuery<NoeudSituation> queryNoeud = cbNoeud.createQuery(NoeudSituation.class);
        Root<NoeudSituation> noeudSituationRoot = queryNoeud.from(NoeudSituation.class);

        queryNoeud.select(noeudSituationRoot).where(
                cbNoeud.equal(noeudSituationRoot.get("idNoeudTheorique"), idNoeudTheorique),
                cbNoeud.equal(noeudSituationRoot.get("formatSolution"), this.formatSolution),
                cbNoeud.equal(noeudSituationRoot.get("profilJoueur"), profilJoueur)
        );

        return session.createQuery(queryNoeud).getResultList();
    }

    /**
     * sélectionne les noeuds Action sur la base de leur noeud abstrait + profil du joueur
     * @param noeudIdentique si false et aucun résultat, on va sélectionner le noeud le plus proche
     */
    private List<NoeudAction> trouverNoeudActionBDD(
            long idNoeudTheorique, boolean noeudIdentique, ProfilJoueur profilJoueur) {
        CriteriaBuilder cbNoeud = session.getCriteriaBuilder();
        CriteriaQuery<NoeudAction> queryNoeud = cbNoeud.createQuery(NoeudAction.class);
        Root<NoeudAction> noeudActionRoot = queryNoeud.from(NoeudAction.class);
        Join<NoeudAction, NoeudSituation> joinSituation = noeudActionRoot.join("noeudSituation");

        queryNoeud.select(noeudActionRoot).where(
                cbNoeud.equal(noeudActionRoot.get("idNoeudTheorique"), idNoeudTheorique),
                cbNoeud.equal(joinSituation.get("formatSolution"), this.formatSolution),
                cbNoeud.equal(joinSituation.get("profilJoueur"), profilJoueur)
        );

        List<NoeudAction> noeudsCorrespondants = session.createQuery(queryNoeud).getResultList();
        // si un noeud trouvé ou bien si on cherche un noeud identique
        if (!(noeudsCorrespondants.isEmpty()) || noeudIdentique) {
            return noeudsCorrespondants;
        }

        else {
            return noeudsPlusProches(idNoeudTheorique, profilJoueur);
        }
    }

    /**
     * cherche dans la BDD le noeud théorique le plus proches d'un noeud donné
     * uniquement grâce à l'arbre abstrait
     * @param idNoeudTheorique id du noeud abstrait cherché
     * @param profilJoueur profil du joueur
     * @return  les noeudsAction de la BDD correspondant à ce noeud théorique
     */
    private List<NoeudAction> noeudsPlusProches(long idNoeudTheorique, ProfilJoueur profilJoueur) {
        List<NoeudAction> noeudsCorrespondants = new ArrayList<>();

        // si on trouve pas directement le noeud cherché on va faire appel à l'arbre
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        List<NoeudAbstrait> noeudsPlusProches = arbreAbstrait.noeudsPlusProches(noeudAbstrait);

        int index = 0;
        while (noeudsCorrespondants.isEmpty()) {
            // important, il faut récréer une requête différente
            CriteriaBuilder cbNoeudDifferent = session.getCriteriaBuilder();
            CriteriaQuery<NoeudAction> queryNoeudDifferent = cbNoeudDifferent.createQuery(NoeudAction.class);
            Root<NoeudAction> noeudActionDifferent = queryNoeudDifferent.from(NoeudAction.class);
            Join<NoeudAction, NoeudSituation> joinSituationNoeudDifferent =
                    noeudActionDifferent.join("noeudSituation");

            if (index >= noeudsPlusProches.size())
                throw new RuntimeException("Aucune range trouvée pour : " + noeudAbstrait);

            NoeudAbstrait noeudTeste = noeudsPlusProches.get(index++);
            queryNoeudDifferent.select(noeudActionDifferent).where(
                    cbNoeudDifferent.equal(noeudActionDifferent.get("idNoeudTheorique"), noeudTeste.toLong()),
                    cbNoeudDifferent.equal(joinSituationNoeudDifferent.get("formatSolution"), this.formatSolution),
                    cbNoeudDifferent.equal(joinSituationNoeudDifferent.get("profilJoueur"), profilJoueur)
            );
            noeudsCorrespondants = session.createQuery(queryNoeudDifferent).getResultList();
        }

        return noeudsCorrespondants;

    }

    protected void ouvrirSession() {
        session = ConnexionBDD.ouvrirSession();
    }

    protected void fermerSession() {
        ConnexionBDD.fermerSession(session);
        session = null;
    }
}
