package analyzor.modele.arbre;

import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.*;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.OppositionRange;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;

import java.util.*;

/**
 * à partir d'un échantillon va récupérer les ranges moyennes
 */
public class RecupRangeIso extends RecuperateurRange {
    private final List<RangeIso> rangesHero;
    private final ProfilJoueur profilJoueur;
    private final List<HashMap<Integer, RangeIso>> listeRangesVillains;
    public RecupRangeIso(FormatSolution formatSolution, ProfilJoueur profilJoueur) {
        super(formatSolution);
        this.profilJoueur = profilJoueur;
        this.rangesHero = new ArrayList<>();
        this.listeRangesVillains = new ArrayList<>();
    }

    // méthode pour récupérer les ranges depuis un échantillon d'entrées

    public OppositionRange recupererRanges(List<Entree> echantillonEntrees) {
        this.rangesHero.clear();
        this.listeRangesVillains.clear();

        this.ouvrirSession();
        recupererToutesLesRanges(echantillonEntrees);
        this.fermerSession();

        // on moyennise les ranges récupérées
        OppositionRange oppositionRange = new OppositionRange();
        moyenniserLesRanges(oppositionRange);

        return oppositionRange;
    }

    /**
     * on récupère les ranges associées à chaque échantillon
     */
    private void recupererToutesLesRanges(List<Entree> entrees) {
        // on récupère les ranges pour chaque entrée de l'échantillon
        for (Entree entree : entrees) {
            // todo PRODUCTION log critique à supprimer
            logger.trace("Entrée de l'échantillon traitée : " + entree.getId());

            List<Entree> entreesPrecedentes = recupererEntreesPrecedentes(entree);

            Joueur hero = entree.getJoueur();
            // on trouve les villains qui vont jouer après et on initialise leur range
            List<Joueur> villainsActifs = trouverVillainsActifs(entree);
            // todo PRODUCTION log critique à supprimer
            logger.trace("Villains actifs trouvés : " + villainsActifs.size());
            // cas où tout les villains ont foldé, on ne prendra pas en compte
            if (villainsActifs.isEmpty()) continue;
            ajouterRanges(entreesPrecedentes, hero, villainsActifs);
        }
    }

    private void moyenniserLesRanges(OppositionRange oppositionRange) {
        RangeIso rangeHero = moyenniserRange(rangesHero);
        // todo PRODUCTION log critique à supprimer
        logger.trace("RANGE HERO TROUVEE : " + rangeHero);
        oppositionRange.setRangeHero(rangeHero);

        for (int indexVillain : listeRangesVillains.getFirst().keySet()) {
            List<RangeIso> rangesVillain = new ArrayList<>();
            for (HashMap<Integer, RangeIso> mapRanges : listeRangesVillains) {
                RangeIso rangeVillain = mapRanges.get(indexVillain);
                // cas qui va arriver quand on a que des folds après root à 3 joueurs et plus
                // car pas d'action du dernier joueur
                if (rangeVillain == null) {
                    // todo PRODUCTION log critique à supprimer
                    logger.debug("Pas autant de joueurs dans chaque échantillon");
                    continue;
                }
                rangesVillain.add(rangeVillain);
            }
            RangeIso rangeMoyenne = moyenniserRange(rangesVillain);
            // todo PRODUCTION log critique à supprimer
            logger.trace("RANGE VILLAIN TROUVEE : " + rangeMoyenne);
            oppositionRange.addRangeVillain(rangeMoyenne);
        }
    }

    protected void ajouterRanges(List<Entree> entreesPrecedentes, Joueur hero, List<Joueur> villainsActifs) {
        // todo si le joueur a fold, sa range doit être vide
        // d'abord on initialise les ranges
        RangeIso rangeHero = new RangeIso();
        rangeHero.remplir();
        this.rangesHero.add(rangeHero);

        // on enregistre les joueurs dans l'ordre des premieres actions comme ça on peut comparer les échantillons
        HashMap<Joueur, Integer> positions = new HashMap<>();
        HashMap<Integer, RangeIso> rangesVillains = new HashMap<>();

        int compte = 0;
        for (Joueur villain : villainsActifs) {
            RangeIso nouvelleRange = new RangeIso();
            nouvelleRange.remplir();

            rangesVillains.put(compte, nouvelleRange);
            positions.put(villain, compte);
            compte++;
        }

        // puis on multiplie ces ranges au fur et à mesure des actions
        for (Entree entree : entreesPrecedentes) {
            Joueur joueurAction = entree.getJoueur();
            boolean entreeHero = (joueurAction.equals(hero));

            if (!(villainsActifs.contains(joueurAction))) continue;

            logger.trace("Recherche de range relative pour entree : " + entree.getId());
            RangeIso rangeAction = trouverRangeRelative(entree, entreeHero);
            // todo PRODUCTION log critique à supprimer
            logger.trace("Détail de la range : " + rangeAction);

            if (entreeHero) rangeHero.multiplier(rangeAction);
            else {
                Integer indexJoueur = positions.get(joueurAction);
                RangeIso rangePrecedente = rangesVillains.get(indexJoueur);
                if (rangePrecedente == null) continue;
                rangePrecedente.multiplier(rangeAction);
            }
        }

        this.listeRangesVillains.add(rangesVillains);
    }

    public RangeIso trouverRangeRelative(Entree entree, boolean heroJoue) {
        ProfilJoueur profilCherche;
        if (heroJoue) {
            profilCherche = profilJoueur;
        }
        else profilCherche = ObjetUnique.selectionnerVillain();

        // noeud identique est false car parfois, une range n'existera pas car hero aura pris trop de fois l'action précédente concernée
        // si on met true, ça va buguer ce qui est logique
        RangeSauvegardable rangeTrouvee = null;
        try {
            rangeTrouvee =  selectionnerRange(entree.getIdNoeudTheorique(), entree.getCodeStackEffectif(),
                            entree.getPotTotal(), entree.getPotBounty(), entree.getBetSize(), profilCherche, false);
        }
        catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de range de l'entrée : " + entree.getId(), e);
        }

        if ((!(rangeTrouvee instanceof RangeIso)))
            // todo PRODUCTION log critique à encrypter
            throw new RuntimeException("La range trouvée n'est pas une RangeIso : " + rangeTrouvee);

        return (RangeIso) rangeTrouvee;
    }

    protected RangeIso moyenniserRange(List<RangeIso> listeRanges) {
        // todo PRODUCTION log critique à supprimer
        logger.trace("Moyennisation des ranges");
        // on construit une range moyenne
        RangeIso rangeMoyenne = new RangeIso();
        int nEchantillons = listeRanges.size();
        for (RangeIso rangeIso : listeRanges) {
            // todo PRODUCTION log critique à supprimer
            logger.trace("Range incrémentée : " + rangeIso);
            for (ComboIso comboIso : GenerateurCombos.combosIso) {
                float valeur = rangeIso.getValeur(comboIso);
                rangeMoyenne.incrementerCombo(comboIso, valeur / nEchantillons);
            }
        }
        return rangeMoyenne;
    }

    // todo PRODUCTION A SUPPRIMER
    // utilisé pour débug de la récupération de range
    public static void main(String[] args) {
        long idFormatSolution = 1;

        NoeudAbstrait noeudTheorique = new NoeudAbstrait(6, TourMain.Round.PREFLOP);
        noeudTheorique.ajouterAction(Move.ALL_IN);
        noeudTheorique.ajouterAction(Move.FOLD);
        noeudTheorique.ajouterAction(Move.FOLD);
        noeudTheorique.ajouterAction(Move.FOLD);
        noeudTheorique.ajouterAction(Move.FOLD);

        // récupération du FormatSolution

        Session sessionFormatSolution = ConnexionBDD.ouvrirSession();
        CriteriaBuilder builderFormatSolution = sessionFormatSolution.getCriteriaBuilder();

        CriteriaQuery<FormatSolution> formatSolutionCriteria = builderFormatSolution.createQuery(FormatSolution.class);
        Root<FormatSolution> formatSolutionRoot = formatSolutionCriteria.from(FormatSolution.class);

        formatSolutionCriteria.select(formatSolutionRoot).where(
                builderFormatSolution.equal(formatSolutionRoot.get("id"), idFormatSolution)
        );

        FormatSolution formatSolution = sessionFormatSolution.createQuery(formatSolutionCriteria).getSingleResultOrNull();

        ConnexionBDD.fermerSession(sessionFormatSolution);

        ProfilJoueur profilJoueur = ObjetUnique.selectionnerVillain();
        RecupRangeIso recupRangeIso = new RecupRangeIso(formatSolution, profilJoueur);

        // récupération des entrées correspondant au noeud

        Session session = ConnexionBDD.ouvrirSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Entree> entreeCriteria = builder.createQuery(Entree.class);
        Root<Entree> entreeRoot = entreeCriteria.from(Entree.class);

        Join<Entree, Joueur> joueurJoin = entreeRoot.join("joueur");
        entreeRoot.fetch("joueur", JoinType.INNER);
        Join<Entree, TourMain> tourMainJoin = entreeRoot.join("tourMain");
        Join<TourMain, MainEnregistree> mainJoin = tourMainJoin.join("main");

        entreeCriteria.select(entreeRoot).where(
                builder.equal(entreeRoot.get("idNoeudTheorique"), noeudTheorique.toLong())
        );

        List<Entree> listEntrees = session.createQuery(entreeCriteria).setMaxResults(100).getResultList();

        ConnexionBDD.fermerSession(session);


        recupRangeIso.recupererRanges(listEntrees);
    }

}
