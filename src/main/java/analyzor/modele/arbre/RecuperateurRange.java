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


public class RecuperateurRange {
    protected final Logger logger = LogManager.getLogger(RecuperateurRange.class);
    Session session;
    private final FormatSolution formatSolution;
    public RecuperateurRange(FormatSolution formatSolution) {

        this.formatSolution = formatSolution;
    }





    public RangeSauvegardable selectionnerRange(long idNoeudTheorique, long codeStackEffectif, float pot,
                                                float potBounty, float betSize, ProfilJoueur profilJoueur,
                                                boolean noeudIdentique) {
        boolean sessionDejaOuverte = (session != null);
        if (!sessionDejaOuverte) this.ouvrirSession();

        NoeudAction noeudPlusProche = noeudActionPlusProche(idNoeudTheorique, codeStackEffectif, pot, potBounty, betSize,
                profilJoueur, noeudIdentique);


        if (noeudPlusProche == null)
            throw new RuntimeException("RR1");
        RangeSauvegardable rangeTrouvee = noeudPlusProche.getRange();

        if (!sessionDejaOuverte) this.fermerSession();
        return rangeTrouvee;
    }


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


        query.orderBy(cb.asc(entreeRoot.get("numAction")));


        List<Joueur> villains = new ArrayList<>();

        for (Entree entreeVillain : session.createQuery(query).getResultList()) {
            Joueur villain = entreeVillain.getJoueur();
            Long idNoeudTheorique = entreeVillain.getIdNoeudTheorique();
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);

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


        query.orderBy(cb.asc(entreeRoot.get("numAction")));

        return session.createQuery(query).getResultList();
    }


    private HashMap<NoeudMesurable, Float> distanceSituations(SituationStackPotBounty situationNoeudCherche,
                                                              List<? extends NoeudMesurable> noeudSituations) {
        HashMap<NoeudMesurable, Float> distances = new HashMap<>();

        EnregistrementNormalisation enregistrementNormalisation = new EnregistrementNormalisation();

        for (NoeudMesurable noeudTrouve : noeudSituations) {

            SituationStackPotBounty situationNoeudBDD = new SituationStackPotBounty(
                    BuilderStackEffectif.getStacksEffectifs(noeudTrouve.getCodeStackEffectif()),
                    noeudTrouve.getPot(),
                    noeudTrouve.getPotBounty()
            );

            situationNoeudBDD.normalisationActivee(false);
            MinMaxCalculSituation minMaxCalculSituation;
            try {

                minMaxCalculSituation = enregistrementNormalisation.recupererMinMax(
                        noeudTrouve.getIdFormatSolution(),
                        noeudTrouve.getIdNoeudSituation()
                );
            }
            catch (Exception e) {
                throw new ErreurCritique("RR2");
            }


            situationNoeudCherche.activerMinMaxNormalisation(
                    minMaxCalculSituation.getMinValeurs(),
                    minMaxCalculSituation.getMaxValeurs()
            );

            float distance = situationNoeudCherche.distance(situationNoeudBDD);
            distances.put(noeudTrouve, distance);
        }

        return distances;
    }


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

        if (!(noeudsCorrespondants.isEmpty()) || noeudIdentique) {
            return noeudsCorrespondants;
        }

        else {
            return noeudsPlusProches(idNoeudTheorique, profilJoueur);
        }
    }


    private List<NoeudAction> noeudsPlusProches(long idNoeudTheorique, ProfilJoueur profilJoueur) {
        List<NoeudAction> noeudsCorrespondants = new ArrayList<>();


        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(idNoeudTheorique);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        List<NoeudAbstrait> noeudsPlusProches = arbreAbstrait.noeudsPlusProches(noeudAbstrait);

        int index = 0;
        while (noeudsCorrespondants.isEmpty()) {

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
