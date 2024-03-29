package analyzor.modele.extraction;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.Joueur;
import analyzor.modele.parties.Move;
import analyzor.modele.simulation.TablePoker;

public class TableImport extends TablePoker {
    
    private static final float MIN_MONTANT_ALL_IN = 4;
    
    private static final float MAX_PCT_RAISE = 0.8f;
    public TableImport(float montantBB) {
        super(montantBB);
    }

    
    public void ajouterJoueur(String nom, int siege, float stack, float bounty, Joueur joueurBDD) {
        JoueurTable nouveauJoueur = new JoueurTable(nom, siege, stack, bounty, joueurBDD);
        super.ajouterJoueur(nom, nouveauJoueur);
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

    
    public JoueurTable setJoueur(String nomJoueur) {
        this.joueurActuel = selectionnerJoueur(nomJoueur);
        return joueurActuel;
    }

    
    public Action ajouterAction(String nomJoueur, Move move, float betSize, boolean betTotal) {
        JoueurTable joueurAction = selectionnerJoueur(nomJoueur);
        float betSupplementaire = normaliserBetSize(joueurAction, betSize, betTotal);

        float montantBetTotal = betSupplementaire + joueurAction.investiCeTour();

        
        
        if ((montantBetTotal / joueurAction.getStackInitial()) > MAX_PCT_RAISE) {
            move = Move.ALL_IN;
        }

        
        
        if (move == Move.ALL_IN && ((montantBetTotal / montantBB) < MIN_MONTANT_ALL_IN)) {
            move = Move.RAISE;
        }

        super.ajouterAction(joueurAction, move, betSupplementaire);

        return new Action(move, betSupplementaire);
    }
}
