package analyzor.modele.extraction;

import analyzor.modele.parties.Joueur;
import analyzor.modele.simulation.TablePoker;

public class TableImport extends TablePoker {
    public TableImport(int montantBB) {
        super(montantBB, true);
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
        float valeurReelle = joueurTable.setAnte(valeurAnte);
        potTable.incrementer(valeurReelle);
    }

    public void ajouterBlindes(String nomJoueurBB, String nomJoueurSB) {
        JoueurTable joueurBB = selectionnerJoueur(nomJoueurBB);
        int montantPayeBB = (int) joueurBB.ajouterMise(this.montantBB);

        JoueurTable joueurSB;
        int montantPayeSB;
        if (nomJoueurSB != null) {
            joueurSB = selectionnerJoueur(nomJoueurSB);
            montantPayeSB = (int) joueurSB.ajouterMise(((float) this.montantBB / 2));
        }

        else {
            montantPayeSB = 0;
        }

        potTable.incrementer(montantPayeSB + montantPayeBB);
        potTable.setDernierBet(Math.max(montantPayeSB, montantPayeBB));
    }
}
