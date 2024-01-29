package analyzor.modele.extraction;

import analyzor.modele.parties.Joueur;
import analyzor.modele.simulation.TablePoker;

public class TableImport extends TablePoker {
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
}
