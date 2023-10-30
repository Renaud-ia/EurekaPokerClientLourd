package analyzor.vue.gestionformat;

import analyzor.vue.donnees.DAOFormat;

import java.util.HashMap;

public class PanneauLignesCalcul extends PanneauActualisable {
    private HashMap<Integer, LigneCalcul> lignesCalcul;

    protected PanneauLignesCalcul(FenetreFormat fenetreParente) {
        super(fenetreParente);
    }

    @Override
    protected void ajouterLigne(DAOFormat.InfosFormat infosFormat) {
        LigneCalcul nouvelleLigne = new LigneCalcul(this, infosFormat);
        lignesCalcul.put(infosFormat.getIndexAffichage(), nouvelleLigne);
        this.add(nouvelleLigne);
        super.ajouterEspace();
        this.repaint();
    }

    @Override
    protected void modifierLigne(DAOFormat.InfosFormat infosFormat) {
        LigneCalcul ligneModifiee = lignesCalcul.get(infosFormat.getIndexAffichage());
        ligneModifiee.actualiser();
        this.repaint();
    }

    @Override
    protected void supprimerLigne(int index) {
        LigneCalcul ligneSupprimee = lignesCalcul.get(index);
        this.remove(ligneSupprimee);
        lignesCalcul.remove(index);
        this.repaint();
    }

    // doit avoir accès à fenêtre
    protected void setModeCalcul(boolean active, LigneCalcul ligneClique) {
        // on réactive/désactive les boutons calculer de toutes les autres lignes calcul
        boolean modeAutresBoutons = !active;
        for (LigneCalcul ligneCalcul : lignesCalcul.values()) {
            if (ligneCalcul == ligneClique) continue;
            ligneCalcul.setBoutonCalculer(modeAutresBoutons);
        }
        fenetreParente.setModeCalcul(active);
    }

}
