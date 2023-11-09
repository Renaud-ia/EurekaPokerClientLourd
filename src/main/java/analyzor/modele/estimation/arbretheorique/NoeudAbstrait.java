package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * produit un identifiant unique par action
 * labellise pendant l'import
 * à partir de l'identifiant regénère la situation avec les infos : nombre de joueurs, position etc
 * ne prend pas en compte le cas où SB est absente (= marginal) mais va le transposer
 * compare les distances entre séquences d'action -> permet de mapper des séquences non existantes dans l'arbre
 * vers la situation la plus proche
 */
public class NoeudAbstrait {
    private int joueursInitiaux;
    private int joueursActifs;
    private int joueursRestants;
    private int rangAction;
    private LinkedList<Move> suiteMoves;
    private boolean noeudValide;
    private TourMain.Round round;
    public NoeudAbstrait(int joueursInitiaux, TourMain.Round round) {
        initialiserNoeud(joueursInitiaux, round);
    }


    public NoeudAbstrait(long intUnique) {
        if (intUnique == -1) {
            noeudValide = false;
            return;
        }

        joueursInitiaux = 0;
    }

    private void initialiserNoeud(int joueursInitiaux, TourMain.Round round) {
        this.joueursInitiaux = joueursInitiaux;
        this.joueursActifs = joueursInitiaux;
        this.joueursRestants = joueursInitiaux - 1;
        noeudValide = true;
        this.round = round;
        this.suiteMoves = new LinkedList<>();
    }

    public void ajouterAction(Move move) {
        //todo
        this.suiteMoves.addFirst(move);
        if (move == Move.FOLD) {
            this.joueursActifs--;
            this.joueursRestants--;
        }
        if (move == Move.ALL_IN) {
            this.joueursRestants--;
        }
    }

    public boolean isLeaf() {
        //todo
        return true;
    }

    protected int distanceNoeud(NoeudAbstrait autreNoeud) {
        if (!autreNoeud.noeudValide) throw new IllegalArgumentException("Le noeud n'est pas valide");
        int distance = 0;

        // du critère le moins discriminant au plus discriminant
        // todo vérifier que ça correspond bien à ce qu'on attend
        distance += Math.abs(rangAction - autreNoeud.rangAction);
        // on compare les trois dernières actions
        for (int i = 0; i < this.suiteMoves.size(); i++) {
            Move moveNoeud = this.suiteMoves.get(i);
            Move autreMove;
            if (i < autreNoeud.suiteMoves.size()) {
                autreMove = autreNoeud.suiteMoves.get(i);
            }
            else autreMove = Move.CHECK;
            distance += moveNoeud.distance(autreMove) * 10;
        }
        distance += Math.abs(joueursActifs - autreNoeud.joueursActifs) * 100;
        distance += Math.abs(joueursRestants - autreNoeud.joueursRestants) * 1000;
        // todo : n'est peut être pas aussi discriminant
        distance += round.distance(autreNoeud.round) * 10000;

        return distance;
    }

    public long toLong() {
        //todo
        // juste nombre joueurs initiaux + round + une suite d'actions encodée : possibilité de stocker 20 actions
        // si on déborde on genère -1 => noeud invalide
        return -1;
    }

    public boolean isValide() {
        return noeudValide;
    }

    public TourMain.Round roundActuel() {
        return round;
    }

    public Move getMove() {
        if (suiteMoves.isEmpty()) return null;
        return suiteMoves.get(0);
    }

    @Override
    public boolean equals(Object noeudAbstrait) {
        if (!(noeudAbstrait instanceof NoeudAbstrait)) return false;
        return this.toLong() == ((NoeudAbstrait) noeudAbstrait).toLong();
    }

    public int getRang() {
        return rangAction;
    }

    public NoeudAbstrait copie() {
        //todo
        return null;
    }
}
