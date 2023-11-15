package analyzor.modele.arbre;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Joueur;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.OppositionRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * à partir d'un échantillon va récupérer les ranges moyennes
 */
public class RecupRangeIso extends RecuperateurRange {
    private List<RangeIso> rangesHero;
    private final List<List<RangeIso>> listeRangesVillains;
    public RecupRangeIso(FormatSolution formatSolution) {
        super(formatSolution);
        listeRangesVillains = new ArrayList<>();
    }

    public OppositionRange recupererRanges(List<Entree> echantillonEntrees) {
        this.ouvrirSession();

        // on récupère les ranges pour chaque entrée de l'échantillon
        for (Entree entree : echantillonEntrees) {
            List<Entree> entreesPrecedentes = recupererEntreesPrecedentes(entree);

            Joueur hero = entree.getJoueur();
            // on trouve les villains qui vont jouer après et on initialise leur range
            List<Joueur> villainsActifs = trouverVillainsActifs(entree);
            trouverLesRanges(entreesPrecedentes, hero, villainsActifs);
        }

        this.fermerSession();

        // on moyennise les ranges récupérées
        OppositionRange oppositionRange = new OppositionRange();

        RangeIso rangeHero = moyenniserRange(rangesHero);
        oppositionRange.setRangeHero(rangeHero);

        for (List<RangeIso> rangesVillain : listeRangesVillains) {
            RangeIso rangeMoyenne = moyenniserRange(rangesVillain);
            oppositionRange.addRangeVillain(rangeMoyenne);
        }

        return oppositionRange;
    }

    protected void trouverLesRanges(List<Entree> entreesPrecedentes, Joueur hero, List<Joueur> villainsActifs) {
        // d'abord on initialise les ranges
        RangeIso rangeHero = new RangeIso();
        rangeHero.remplir();
        this.rangesHero.add(rangeHero);

        HashMap<Joueur, RangeIso> rangesVillains = new HashMap<>();

        for (Joueur villain : villainsActifs) {
            RangeIso nouvelleRange = new RangeIso();
            nouvelleRange.remplir();

            rangesVillains.put(villain, nouvelleRange);
        }
        this.listeRangesVillains.add((List<RangeIso>) rangesVillains.values());

        // puis on multiplie ces ranges au fur et à mesure des actions
        for (Entree entree : entreesPrecedentes) {
            RangeIso rangeAction = trouverRangeRelative(entree);

            Joueur joueurAction = entree.getJoueur();
            if (joueurAction.equals(hero)) rangeHero.multiplier(rangeAction);
            else {
                RangeIso rangePrecedente = rangesVillains.get(joueurAction);
                if (rangePrecedente == null) continue;
                rangePrecedente.multiplier(rangeAction);
            }
        }
    }

    private RangeIso trouverRangeRelative(Entree entree) {
        RangeSauvegardable rangeTrouvee =
                selectionnerRange(entree.getIdNoeudTheorique(), entree.getStackEffectif(),
                entree.getPotTotal(), entree.getPotBounty(), entree.getBetSize());

        if ((!(rangeTrouvee instanceof RangeIso)))
            throw new RuntimeException("La range trouvée n'est pas une RangeIso");

        return (RangeIso) rangeTrouvee;
    }

    protected RangeIso moyenniserRange(List<RangeIso> listeRanges) {
        // on construit une range moyenne
        RangeIso rangeMoyenne = new RangeIso();
        int nEchantillons = listeRanges.size();
        for (RangeIso rangeIso : listeRanges) {
            for (ComboIso comboIso : GenerateurCombos.combosIso) {
                float valeur = rangeIso.getValeur(comboIso);
                rangeMoyenne.incrementerCombo(comboIso, valeur / nEchantillons);
            }
        }
        return rangeMoyenne;
    }
}
