package analyzor.modele.arbre;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.poker.RangeIso;

import java.util.ArrayList;
import java.util.List;




public class ExplorateurArbre {
    private ArbreAbstrait arbreAbstrait;
    public ExplorateurArbre(ArbreAbstrait arbreAbstrait) {
        this.arbreAbstrait = arbreAbstrait;
    }
    
    
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
