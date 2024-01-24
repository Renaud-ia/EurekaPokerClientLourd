package analyzor.modele.extraction;

import analyzor.modele.parties.Joueur;
import analyzor.modele.simulation.TablePoker;

public class TableImport extends TablePoker {
    public TableImport(int montantBB) {
        super(montantBB);
    }

    /**
     * méthode appelée en premier
     * ajout des joueurs par import mains
     * le stack est indiqué en absolu et pas en BB
     */
    public void ajouterJoueur(String nom, int siege, int stack, float bounty, Joueur joueurBDD) {
        JoueurTable nouveauJoueur = new JoueurTable(nom, siege, stack, bounty, joueurBDD);
        mapJoueursNom.put(nom, nouveauJoueur);
    }

    public void ajouterGains(String nomJoueur, int gains) {
        JoueurTable joueur = selectionnerJoueur(nomJoueur);
        joueur.setGains(gains);
    }

    public void ajouterCartes(String nomJoueur, int combo) {
        JoueurTable joueur = selectionnerJoueur(nomJoueur);
        joueur.setCartes(combo);
    }

    public float getStackJoueur(String nomJoueur) {
        JoueurTable joueurTable = selectionnerJoueur(nomJoueur);
        return joueurTable.getStackActuel() / montantBB;
    }

    public void ajouterAnte(String nomJoueur, int valeurAnte) {
        JoueurTable joueurTable = selectionnerJoueur(nomJoueur);
        this.ajouterAnte(joueurTable, valeurAnte);
    }

    public void ajouterBlindes(String nomJoueurBB, String nomJoueurSB) {
        JoueurTable joueurBB = selectionnerJoueur(nomJoueurBB);

        JoueurTable joueurSB = null;
        int montantPayeSB;
        if (nomJoueurSB != null) {
            joueurSB = selectionnerJoueur(nomJoueurSB);
        }

        this.ajouterBlindes(joueurBB, joueurSB);
    }

    public int getMontantBB() {
        return montantBB;
    }
}
