package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Joueur;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.OppositionRange;

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
            logger.trace("Entrée de l'échantillon : " + entree.getId());

            List<Entree> entreesPrecedentes = recupererEntreesPrecedentes(entree);

            Joueur hero = entree.getJoueur();
            // on trouve les villains qui vont jouer après et on initialise leur range
            List<Joueur> villainsActifs = trouverVillainsActifs(entree);
            logger.trace("Villains actifs trouvés : " + villainsActifs.size());
            // cas où tout les villains ont foldé, on ne prendra pas en compte
            if (villainsActifs.isEmpty()) continue;
            ajouterRanges(entreesPrecedentes, hero, villainsActifs);
        }
    }

    private void moyenniserLesRanges(OppositionRange oppositionRange) {
        RangeIso rangeHero = moyenniserRange(rangesHero);
        logger.debug("RANGE HERO TROUVEE : " + rangeHero);
        oppositionRange.setRangeHero(rangeHero);

        for (int indexVillain : listeRangesVillains.getFirst().keySet()) {
            List<RangeIso> rangesVillain = new ArrayList<>();
            for (HashMap<Integer, RangeIso> mapRanges : listeRangesVillains) {
                RangeIso rangeVillain = mapRanges.get(indexVillain);
                // cas qui va arriver quand on a que des folds après root à 3 joueurs et plus
                // car pas d'action du dernier joueur
                if (rangeVillain == null) {
                    logger.warn("Pas autant de joueurs dans chaque échantillon");
                    continue;
                }
                rangesVillain.add(rangeVillain);
            }
            RangeIso rangeMoyenne = moyenniserRange(rangesVillain);
            logger.debug("RANGE VILLAIN TROUVEE : " + rangeMoyenne);
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
            RangeIso rangeAction = trouverRangeRelative(entree);
            logger.trace("Range relative trouvée pour Entree n°" + entree.getId());
            logger.trace("Détail de la range : " + rangeAction);

            Joueur joueurAction = entree.getJoueur();
            if (joueurAction.equals(hero)) rangeHero.multiplier(rangeAction);
            else {
                Integer indexJoueur = positions.get(joueurAction);
                RangeIso rangePrecedente = rangesVillains.get(indexJoueur);
                if (rangePrecedente == null) continue;
                rangePrecedente.multiplier(rangeAction);

                logger.trace("Range villain après multipliation :" + rangePrecedente);
            }
        }

        this.listeRangesVillains.add(rangesVillains);
    }

    private RangeIso trouverRangeRelative(Entree entree) {
        // noeud identique est false car parfois, une range n'existera pas car hero aura pris trop de fois l'action précédente concernée
        // si on met true, ça va buguer ce qui est logique
        RangeSauvegardable rangeTrouvee =
                selectionnerRange(entree.getIdNoeudTheorique(), entree.getStackEffectif(),
                entree.getPotTotal(), entree.getPotBounty(), entree.getBetSize(), profilJoueur, false);

        if ((!(rangeTrouvee instanceof RangeIso)))
            throw new RuntimeException("La range trouvée n'est pas une RangeIso");

        return (RangeIso) rangeTrouvee;
    }

    protected RangeIso moyenniserRange(List<RangeIso> listeRanges) {
        logger.trace("Moyennisation des ranges");
        // on construit une range moyenne
        RangeIso rangeMoyenne = new RangeIso();
        int nEchantillons = listeRanges.size();
        for (RangeIso rangeIso : listeRanges) {
            logger.trace("Range incrémentée : " + rangeIso);
            for (ComboIso comboIso : GenerateurCombos.combosIso) {
                float valeur = rangeIso.getValeur(comboIso);
                rangeMoyenne.incrementerCombo(comboIso, valeur / nEchantillons);
            }
        }
        return rangeMoyenne;
    }

}
