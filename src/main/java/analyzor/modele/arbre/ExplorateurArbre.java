package analyzor.modele.arbre;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.RangeIso;

import java.util.ArrayList;
import java.util.List;

// va convertir n'importe quelle action en son équivalent

// todo : attention on doit tenir compte du format solution de l'arbre abstrait
public class ExplorateurArbre {
    private ArbreAbstrait arbreAbstrait;
    public ExplorateurArbre(ArbreAbstrait arbreAbstrait) {
        this.arbreAbstrait = arbreAbstrait;
    }
    // ne peut marcher que pour flop, va parcourir l'arbre en sens inverse
    // todo : on veut aussi les ranges de Villain
    public RangeIso rangeIso(NoeudPreflop noeudPreflop) {
        return new RangeIso();
    }

    public NoeudAction getNoeudReel(Entree entree) {
        return null;
    }

    public List<NoeudPreflop> getNoeudsReels(NoeudAbstrait noeudAbstrait) {
        return new ArrayList<>();
    }

    public List<Entree> getEntress(NoeudPreflop noeudPreflop) {
        return new ArrayList<>();
    }
}
