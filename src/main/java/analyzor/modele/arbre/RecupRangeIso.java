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


public class RecupRangeIso extends RecuperateurRange {
    private final List<RangeIso> rangesHero;
    private final ProfilJoueur profilJoueur;
    public RecupRangeIso(FormatSolution formatSolution, ProfilJoueur profilJoueur) {
        super(formatSolution);
        this.profilJoueur = profilJoueur;
        this.rangesHero = new ArrayList<>();
    }

    

    public OppositionRange recupererRanges(List<Entree> echantillonEntrees) {
        this.rangesHero.clear();

        this.ouvrirSession();
        recupererToutesLesRanges(echantillonEntrees);
        this.fermerSession();

        
        OppositionRange oppositionRange = new OppositionRange();
        moyenniserLesRanges(oppositionRange);

        return oppositionRange;
    }

    
    private void recupererToutesLesRanges(List<Entree> entrees) {
        
        for (Entree entree : entrees) {

            List<Entree> entreesPrecedentes = recupererEntreesPrecedentes(entree);

            Joueur hero = entree.getJoueur();
            
            List<Joueur> villainsActifs = null;
            ajouterRanges(entreesPrecedentes, hero, villainsActifs);
        }
    }

    private void moyenniserLesRanges(OppositionRange oppositionRange) {
        RangeIso rangeHero = moyenniserRange(rangesHero);
        oppositionRange.setRangeHero(rangeHero);
    }

    protected void ajouterRanges(List<Entree> entreesPrecedentes, Joueur hero, List<Joueur> villainsActifs) {
        
        
        RangeIso rangeHero = new RangeIso();
        rangeHero.remplir();
        this.rangesHero.add(rangeHero);

        
        for (Entree entree : entreesPrecedentes) {
            Joueur joueurAction = entree.getJoueur();
            boolean entreeHero = (joueurAction.equals(hero));

            RangeIso rangeAction = trouverRangeRelative(entree, entreeHero);

            if (entreeHero) rangeHero.multiplier(rangeAction);
        }
    }

    public RangeIso trouverRangeRelative(Entree entree, boolean heroJoue) {
        ProfilJoueur profilCherche;
        if (heroJoue) {
            profilCherche = profilJoueur;
        }
        else profilCherche = ObjetUnique.selectionnerVillain();

        
        
        RangeSauvegardable rangeTrouvee = null;
        try {
            rangeTrouvee =  selectionnerRange(entree.getIdNoeudTheorique(), entree.getCodeStackEffectif(),
                            entree.getPotTotal(), entree.getPotBounty(), entree.getBetSize(), profilCherche, false);
        }
        catch (Exception e) {
            throw new RuntimeException("RR3" + entree.getId(), e);
        }

        if ((!(rangeTrouvee instanceof RangeIso)))
            throw new RuntimeException("RR4" + rangeTrouvee);

        return (RangeIso) rangeTrouvee;
    }

    protected RangeIso moyenniserRange(List<RangeIso> listeRanges) {
        
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
