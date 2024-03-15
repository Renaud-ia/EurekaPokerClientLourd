package analyzor.modele.extraction;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.Joueur;
import analyzor.modele.parties.Move;
import analyzor.modele.simulation.TablePoker;

public class TableImport extends TablePoker {
    // en dessous de ce montant en BB on considère RAISE et pas ALL-IN
    private static final float MIN_MONTANT_ALL_IN = 4;
    // au dessus de ce pourcentage du stack on considère ALL-IN
    private static final float MAX_PCT_RAISE = 0.8f;
    public TableImport(float montantBB) {
        super(montantBB);
    }

    /**
     * méthode appelée en premier
     * ajout des joueurs par import mains
     * le stack est indiqué en absolu et pas en BB
     */
    public void ajouterJoueur(String nom, int siege, float stack, float bounty, Joueur joueurBDD) {
        JoueurTable nouveauJoueur = new JoueurTable(nom, siege, stack, bounty, joueurBDD);
        mapJoueursNom.put(nom, nouveauJoueur);
    }

    public void ajouterGains(String nomJoueur, float gains) {
        JoueurTable joueur = selectionnerJoueur(nomJoueur);
        joueur.setGains(gains);
    }

    public void ajouterCartes(String nomJoueur, int combo) {
        JoueurTable joueur = selectionnerJoueur(nomJoueur);
        joueur.setCartes(combo);
    }

    public float getStackJoueur(String nomJoueur) {
        JoueurTable joueurTable = selectionnerJoueur(nomJoueur);
        return joueurTable.getStackActuel();
    }

    public void ajouterAnte(String nomJoueur, float valeurAnte) {
        JoueurTable joueurTable = selectionnerJoueur(nomJoueur);
        super.ajouterAnte(joueurTable, valeurAnte);
    }

    @Deprecated
    public void ajouterBlindes(String nomJoueurBB, String nomJoueurSB) {
        JoueurTable joueurBB = selectionnerJoueur(nomJoueurBB);

        JoueurTable joueurSB = null;
        if (nomJoueurSB != null) {
            joueurSB = selectionnerJoueur(nomJoueurSB);
        }

        this.ajouterBlindes(joueurBB, joueurSB);
    }

    public void ajouterBlindes(String nomJoueur, float valeurBlinde) {
        JoueurTable joueurTable = selectionnerJoueur(nomJoueur);
        float montantPaye = joueurTable.setBlinde(valeurBlinde);

        potTable.incrementer(montantPaye);
        if (potTable.getDernierBet() < montantPaye) {
            potTable.setDernierBet(montantPaye);
        }
    }

    public float getMontantBB() {
        return montantBB;
    }

    /**
     * méthode manuelle pour fixer le joueur en cours
     * important car on veut pouvoir calculer le stack effectif avant l'action du joueur
     */
    public JoueurTable setJoueur(String nomJoueur) {
        this.joueurActuel = selectionnerJoueur(nomJoueur);
        return joueurActuel;
    }

    /**
     * surcharge de la méthode d'origine
     * permet d'instaurer des critères sur ALL-IN/RAISE pour meilleure équivalence des actions
     * todo POSTFLOP : penser comment on va gérer ça au flop : surement légèrement différent
     * @param nomJoueur : nom du joueur qui fait l'action
     * @param move : move fait par le joueur
     * @param betSize
     * @param betTotal  : si vrai, c'est l'ensemble des mises jusqu'à présent, si faux c'est la mise complémentaire
     *                  attention le montant est indiqué en absolu et pas en relatif
     * @return le montant normalisé de la mise
     */
    public Move ajouterAction(String nomJoueur, Move move, float betSize, boolean betTotal) {
        JoueurTable joueurAction = selectionnerJoueur(nomJoueur);
        float betSupplementaire = normaliserBetSize(joueurAction, betSize, betTotal);

        float montantBetTotal = betSupplementaire + joueurAction.investiCeTour();

        // à partir d'un certain montant c'est ALL-IN
        // procédure indispensable pour Ipoker car pas d'encodage du ALL-IN
        if ((montantBetTotal / joueurAction.getStackInitial()) > MAX_PCT_RAISE) {
            move = Move.ALL_IN;
        }

        // si le all-in est d'un montant trop faible, on considère comme un raise
        // pas de else car on veut corriger aussi la modification précédente
        if (move == Move.ALL_IN && ((montantBetTotal / montantBB) < MIN_MONTANT_ALL_IN)) {
            move = Move.RAISE;
        }

        super.ajouterAction(joueurAction, move, betSupplementaire);

        return move;
    }
}
