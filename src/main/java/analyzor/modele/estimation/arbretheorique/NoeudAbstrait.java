package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.utils.Bits;

import java.util.LinkedList;

/**
 * produit un identifiant unique par action (de type LONG)
 * labellise pendant l'import
 * à partir de l'identifiant regénère la situation avec les infos : nombre de joueurs, position etc
 * ne prend pas en compte le cas où SB est absente (= marginal) mais va le transposer
 * compare les distances entre séquences d'action -> permet de mapper des séquences
 * non existantes dans l'arbre Abstrait vers la situation la plus proche
 * Construction :
 * 1) par initialisation puis ajout action
 * 2) par id Long
 * IMPORTANT : pour cohérence des actions ALL-IN = ALL-IN
 * détections incorrectes des LEAFS avec ALL-IN car dépend des différents stacks
 */
public class NoeudAbstrait {
    private static final int MAX_JOUEURS = 12;
    private int joueursInitiaux;
    private int joueursActifs;
    private int joueursNonCouches;
    private int rangAction;
    private boolean noeudValide;
    private TourMain.Round round;
    // pour déterminer le rang de l'action
    private int nActionsRang;
    private int nJoueursRang;

    private LinkedList<Move> suiteMoves;

    private final static int N_BITS_MOVE = Bits.bitsNecessaires(Move.nombreMovesUneStreet() + 1);
    private final static int N_BITS_ROUND = Bits.bitsNecessaires(TourMain.Round.nombreRounds());
    private final static int N_BITS_JOUEURS = Bits.bitsNecessaires(MAX_JOUEURS);

    //constructeurs

    public NoeudAbstrait(int joueursInitiaux, TourMain.Round round) {
        initialiserNoeud(joueursInitiaux, round);
    }


    public NoeudAbstrait(long idUnique) {
        if (idUnique == -1) {
            noeudValide = false;
            return;
        }

        // masques pour extraire les informations
        int maskJoueurs = ((1 << N_BITS_JOUEURS) - 1) << N_BITS_ROUND;
        int maskRound = (1 << N_BITS_ROUND) - 1;

        int joueursInitiaux = (int) ((idUnique & maskJoueurs) >> N_BITS_ROUND);
        int intRound = (int) (idUnique & maskRound);

        initialiserNoeud(joueursInitiaux, TourMain.Round.fromInt(intRound));

        long actions = idUnique >> (N_BITS_ROUND + N_BITS_JOUEURS);

        // ensuite on ajoute les actions
        int maskMove = (1 << N_BITS_MOVE) - 1;
        while (actions > 0) {
            Move move = (Move.values()[(int) (actions & maskMove) - 1]);
            this.ajouterAction(move);
            actions = actions >> N_BITS_MOVE;
        }
    }

    // utilisé pour copie
    public NoeudAbstrait(int joueursInitiaux, int joueursActifs, int joueursNonCouches,
                         int rangAction, boolean noeudValide, TourMain.Round round, int nActionsRang,
                         int nJoueursRang, LinkedList<Move> suiteMoves) {
        this.joueursInitiaux = joueursInitiaux;
        this.joueursActifs = joueursActifs;
        this.joueursNonCouches = joueursNonCouches;
        this.rangAction = rangAction;
        this.noeudValide = noeudValide;
        this.round = round;
        this.nActionsRang = nActionsRang;
        this.nJoueursRang = nJoueursRang;
        this.suiteMoves = suiteMoves;
    }

    private void initialiserNoeud(int joueursInitiaux, TourMain.Round round) {
        if (joueursInitiaux <= 0 || joueursInitiaux > MAX_JOUEURS) {
            throw new IllegalArgumentException("Nombre de joueurs invalides");
        }
        this.joueursInitiaux = joueursInitiaux;
        this.joueursActifs = joueursInitiaux;
        this.joueursNonCouches = joueursInitiaux;
        noeudValide = true;
        this.round = round;
        this.suiteMoves = new LinkedList<>();

        nouveauRang();
    }

    private void nouveauRang() {
        nActionsRang = 0;
        nJoueursRang = joueursActifs;
    }

    public void ajouterAction(Move move) {
        this.suiteMoves.addFirst(move);

        if (move == Move.FOLD) {
            joueursNonCouches--;
        }

        if (move == Move.FOLD || move == Move.ALL_IN) {
            this.joueursActifs--;
        }

        if (nActionsRang++ == nJoueursRang) {
            rangAction++;
            nouveauRang();
        }
    }

    public boolean isLeaf() {
        if (joueursActifs == 0) return true;
        else if (joueursNonCouches == 1) return true;
        else if (suiteMoves.size() < joueursInitiaux) return false;
        else {
            // on parcourt les actions précédentes du même tour
            // un joueur au moins doit avoir relancé après un joueur qui est encore actif
            int nJoueurTeste = joueursNonCouches;
            boolean actionActive = false;


            // on enregistre dans le sens inverse
            for (Move move : suiteMoves) {
                if (move == Move.FOLD) continue;
                if ((move == Move.CALL || move == Move.RAISE) && actionActive) {
                    return false;
                }
                if (move == Move.ALL_IN || move == Move.RAISE) {
                    actionActive = true;
                }
                if (--nJoueurTeste == 0) break;
            }
            return true;
        }

    }

    protected int distanceNoeud(NoeudAbstrait autreNoeud) {
        if (!autreNoeud.noeudValide) throw new IllegalArgumentException("Le noeud n'est pas valide");
        int distance = 0;

        // du critère le moins discriminant au plus discriminant
        // todo vérifier que ça correspond bien à ce qu'on attend
        distance += Math.abs(rangAction - autreNoeud.rangAction);
        distance += Math.abs(nActionsRang - autreNoeud.nActionsRang) * 10;
        distance += Math.abs(derniereAction().distance(autreNoeud.derniereAction())) * 100;
        distance += Math.abs(nombreRaiseAllin() - autreNoeud.nombreRaiseAllin()) * 1000;
        distance += Math.abs(joueursNonCouches - autreNoeud.joueursNonCouches) * 10000;
        // todo : n'est peut être pas aussi discriminant
        distance += round.distance(autreNoeud.round) * 100000;

        return distance;
    }

    private Move derniereAction() {
        if (suiteMoves.isEmpty()) return Move.FOLD;
        return suiteMoves.getFirst();
    }


    public long toLong() {
        if (!noeudValide) return -1;

        long longAction = 0L;
        int bitsAjoutes = 0;

        for (Move move : suiteMoves) {
            // important on rajoute 1 pour ne pas avoir zéro
            longAction = (longAction << N_BITS_MOVE) | (move.ordinal() + 1);
            bitsAjoutes += N_BITS_MOVE;
        }

        // IMPORTANT => les actions sont au début donc permet de trier rapidement les actions par valeur du long
        // si on déborde on genère -1 => noeud invalide
        int CAPACITE_LONG = 64;
        if ((bitsAjoutes + N_BITS_ROUND + N_BITS_JOUEURS) >= CAPACITE_LONG) {
            return -1;
        }

        // on met joueurs initiaux en premier car jamais nul
        longAction = (longAction << N_BITS_JOUEURS) | joueursInitiaux;
        longAction = (longAction << N_BITS_ROUND) | round.toInt();

        return longAction;
    }

    public boolean isValide() {
        return noeudValide;
    }

    public TourMain.Round roundActuel() {
        return round;
    }

    public Move getMove() {
        if (suiteMoves.isEmpty()) return null;
        return suiteMoves.getFirst();
    }

    @Override
    public boolean equals(Object noeudAbstrait) {
        if (!(noeudAbstrait instanceof NoeudAbstrait)) return false;
        return this.toLong() == ((NoeudAbstrait) noeudAbstrait).toLong();
    }

    // on ne réécrit pas hashcode car les clés sont des LONG

    public int getRang() {
        return rangAction;
    }

    // utilisé par l'arbre abstrait
    public NoeudAbstrait copie() {
        LinkedList<Move> copiesMoves = new LinkedList<>(suiteMoves);

        return new NoeudAbstrait(joueursInitiaux, joueursActifs, joueursNonCouches, rangAction,
                noeudValide, round, nActionsRang, nJoueursRang, copiesMoves);
    }

    public boolean hasAllin() {
        for (Move move : suiteMoves) {
            if (move == Move.ALL_IN) return true;
        }
        return false;
    }

    public int nombreRaise() {
        int nombreRaise = 0;
        for (Move move : suiteMoves) {
            if (move == Move.RAISE) nombreRaise++;
        }
        return nombreRaise;
    }

    private int nombreRaiseAllin() {
        int nombreRaiseAllIn = 0;
        for (Move move : suiteMoves) {
            if (move == Move.RAISE || move == Move.ALL_IN) nombreRaiseAllIn++;
        }
        return nombreRaiseAllIn;
    }

    @Override
    public String toString() {
        StringBuilder nomAction = new StringBuilder();
        nomAction.append("[").append(round).append(" ").append(joueursInitiaux).append("-way] ");
        nomAction.append(": ");
        if (suiteMoves.isEmpty()) {
            nomAction.append("root");
        }
        else {
            for (int i = suiteMoves.size() -1; i >= 0; i--) {
                Move move = suiteMoves.get(i);
                nomAction.append(move.toString()).append(", ");
            }
        }
        if (isLeaf()) nomAction.append("[leaf]");

        return nomAction.toString();
    }

    // pour feuilles de calcul excel
    public String stringReduite() {
        StringBuilder nomAction = new StringBuilder();
        nomAction.append("(").append(joueursInitiaux).append("p)");
        if (suiteMoves.isEmpty()) {
            nomAction.append("root");
        }
        else {
            for (int i = suiteMoves.size() -1; i >= 0; i--) {
                Move move = suiteMoves.get(i);
                nomAction.append(move.toString().charAt(0)).append(".");
            }
        }

        return nomAction.toString();
    }

    // pour limiter longueur de l'arbre, on empêche plus de maxActions joueurs de rentrer dans le coup
    public boolean maxActionsAtteint(int maxActions) {
        if (round != TourMain.Round.PREFLOP || rangAction > 0) {
            return false;
        }

        // on compte seulement les premières actions
        int compteActions = 0;
        int indexActionsPremierRang = Math.max(0, suiteMoves.size() - joueursInitiaux);

        for (int i = suiteMoves.size() - 1; i >= indexActionsPremierRang; i--) {
            Move move = suiteMoves.get(i);
            if (move != Move.FOLD) {
                compteActions++;
                if (compteActions >= maxActions) {
                    // on regarde si l'action suivante entraine un nouveau rang
                    NoeudAbstrait noeudSuivant = this.copie();
                    noeudSuivant.ajouterAction(Move.RAISE);
                    return noeudSuivant.nActionsRang != 0;
                }
            }
        }

        return false;
    }

    public int nombreActions() {
        return suiteMoves.size();
    }

    public TourMain.Round getRound() {
        return round;
    }
}
