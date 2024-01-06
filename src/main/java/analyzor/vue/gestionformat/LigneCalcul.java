package analyzor.vue.gestionformat;

import analyzor.controleur.WorkerAffichable;
import analyzor.vue.donnees.DTOFormat;

import javax.swing.*;
import java.awt.*;

public class LigneCalcul extends JPanel {
    private final PanneauLignesCalcul panneauParent;
    private final CardLayout cardLayout;
    private final DTOFormat.InfosFormat infosFormat;
    private InfoCalcul ligneInfoCalcul;
    private LancerCalcul ligneLancerCalcul;
    private ProgressionCalcul ligneProgressionCalcul;

    private final Long idBDD;
    // doit avoir accès à son panneau
    // CardLayout avec deux panneaux différents pour mode editionCalcul et mode vueCalcul
    protected LigneCalcul(PanneauLignesCalcul panneauParent, DTOFormat.InfosFormat infosFormat) {
        this.panneauParent = panneauParent;
        this.idBDD = infosFormat.getIdBDD();
        this.infosFormat = infosFormat;
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);
        construireVueLigne();
        setModeActif(false);
    }

    private void construireVueLigne() {
        ligneInfoCalcul = new InfoCalcul(this, infosFormat.getEtat());
        this.add(ligneInfoCalcul, "Info");

        ligneLancerCalcul = new LancerCalcul(this,
                infosFormat.isPreflopCalcule(),
                infosFormat.isFlopCalcule(),
                infosFormat.getNombreParties());
        this.add(ligneLancerCalcul, "Lancer");

        ligneProgressionCalcul = new ProgressionCalcul(this);
        this.add(ligneProgressionCalcul, "Progression");
    }

    // attention, on entre dans la vue Calcul mais on ne lance pas le calcul
    protected void clicCalculer() {
        // on bascule la vue de cette ligne
        // on va désactiver mode sélection et mode édition
        panneauParent.setModeCalcul(true, this);
        setModeActif(true);
    }

    protected void clicOk() {
        // on bascule la vue de cette ligne
        // on rétablit mode sélection
        panneauParent.setModeCalcul(false, this);
        setModeActif(false);
        cardLayout.show(this, "Info");
    }

    protected void clickReinitialiser() {
        //todo fenetre confirmation
        panneauParent.reinitialiserFormat(idBDD);
    }

    /**
     * est ce que le mode calcul est activé pour cette ligne
     * @param active
     */
    public void setModeActif(boolean active) {
        // on switch sur le panneau correspondant de CardLayout
        if (active) {
            cardLayout.show(this, "Lancer");
        }
        else {
            cardLayout.show(this, "Info");
        }
    }

    public void setBoutonCalculer(boolean active) {
        ligneInfoCalcul.setBoutonCalculer(active);
    }

    public void actualiser() {
        ligneInfoCalcul.changerEtat(infosFormat.getEtat());
        ligneLancerCalcul.setParties(infosFormat.getNombreParties());
    }

    public void clicLancerCalcul() {
        panneauParent.lancerCalcul(idBDD, this);
    }

    public void ajouterWorker(WorkerAffichable worker) {
        ligneProgressionCalcul.ajouterWorker(worker);
        cardLayout.show(this, "Progression");
        ligneProgressionCalcul.lancerWorker();
    }

    // appelé si interrompu ou simplement fini
    // on revient juste à l'écran Lancer
    protected void tacheTerminee() {
        cardLayout.show(this, "Lancer");
        this.panneauParent.tacheTerminee();
    }
}
