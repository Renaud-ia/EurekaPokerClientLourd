package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.modele.config.ValeursConfig;
import analyzor.vue.donnees.format.FormCreationFormat;
import analyzor.vue.gestionformat.detailformat.*;
import analyzor.vue.reutilisables.fenetres.FenetreTroisiemeOrdre;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * fenêtre de création des nouveaux formats
 */
public class NouveauFormat extends FenetreTroisiemeOrdre implements ActionListener, ItemListener {
    private final ControleurFormat controleurFormat;
    private final FenetreFormat fenetreFormat;
    private JPanel panneauContenu;
    LigneComboBox choixFormat;
    private FormCreationFormat formCreationFormat;
    private LigneSpinner nombreJoueurs;
    private LigneSpinner buyInMin;
    private LigneSpinner buyInMax;
    private LigneSlider anteMin;
    private LigneSlider anteMax;
    private LigneCheckBox bounty;
    private LigneSlider rakeMin;
    private LigneSlider rakeMax;
    private LigneTexteEditable nomFormat;
    private JButton creerFormat;

    public NouveauFormat(FenetreFormat fenetreFormat, ControleurFormat controleurFormat) {
        super(fenetreFormat, "Création d'un format", true);
        this.controleurFormat = controleurFormat;
        this.fenetreFormat = fenetreFormat;

        initialiser();
        reset();
    }

    private void initialiser() {
        panneauContenu = new JPanel();
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));

        creerFormat = new JButton("Créer");
        creerFormat.addActionListener(this);
    }

    // à l'origine on a juste un choix de format => on construit le reste après
    public void reset() {
        panneauContenu.removeAll();
        choixFormat = new LigneComboBox(
                "Choix du format : ",
                controleurFormat.formatsDisponibles(),
                this
        );
        panneauContenu.add(choixFormat);

        this.add(panneauContenu);
        this.pack();
    }

    /**
     * méthode appelée quand le format est choisi
     * on va afficher toutes les options disponibles pour la création
     * et les boutons de création
     */
    private void deroulerChoix(String formatChoisi) {
        // on crée un nouveau formulaire
        formCreationFormat = new FormCreationFormat(formatChoisi);

        // on redessine tout
        panneauContenu.removeAll();
        panneauContenu.add(choixFormat);

        nombreJoueurs = new LigneSpinner("Nombre de joueurs : ",2, ValeursConfig.MAX_JOUEURS, this);
        panneauContenu.add(nombreJoueurs);

        buyInMin = new LigneSpinner("Buy in minimum : ", 0, 10000, this);
        panneauContenu.add(buyInMin);
        buyInMax = new LigneSpinner("Buy in maximum : ", 0, 10000, this);
        panneauContenu.add(buyInMax);

        if (formCreationFormat.aAnte()) {
            anteMin = new LigneSlider("Ante minimum (en %) : ", 0, 20, this);
            panneauContenu.add(anteMin);
            anteMax = new LigneSlider("Ante maximum (en %) :", 0, 20, this);
            panneauContenu.add(anteMax);
        }

        if (formCreationFormat.bountyExiste()) {
            bounty = new LigneCheckBox("Bounty : ", this);
            panneauContenu.add(bounty);
        }

        if (formCreationFormat.aRake()) {
            rakeMin = new LigneSlider("Rake minimum (en %) : ", 0, 20, this);
            panneauContenu.add(rakeMin);
            rakeMax = new LigneSlider("Rake minimum (en %) :", 0, 20, this);
            panneauContenu.add(rakeMax);
        }

        nomFormat = new LigneTexteEditable("Nom du format", this);
        panneauContenu.add(nomFormat);

        panneauContenu.add(creerFormat);

        remplissageAutomatiqueNom();

        panneauContenu.repaint();
        this.repaint();
        this.pack();
        this.recentrer();
    }

    /**
     * si l'user n'y a pas touché on va générer un nom
     */
    public void remplissageAutomatiqueNom() {
        // si l'user l'a modifié on n'y touche plus
        if (!nomFormat.estModifiable()) {
            return;
        }

        StringBuilder nomGenere = new StringBuilder();
        nomGenere.append(formCreationFormat.getFormat().getPokerFormat().toString()).append(" ");
        nomGenere.append(nombreJoueurs.getValeurSlider()).append(" joueurs ");
        nomGenere.append("[").append(
                buyInMin.getValeurSlider()).append("-").append(buyInMax.getValeurSlider()).append("euros] ");

        if (formCreationFormat.aAnte()) {
            nomGenere.append("(ante : ").append(
                    anteMin.getValeurSlider()).append("-").append(anteMax.getValeurSlider()).append(") ");
        }

        if (formCreationFormat.aRake()) {
            nomGenere.append("(rake : ").append(
                    rakeMin.getValeurSlider()).append("-").append(rakeMax.getValeurSlider()).append(") ");
        }

        if (formCreationFormat.bountyExiste()) {
            if (bounty.estCoche()) {
                nomGenere.append("avec bounty");
            }
            else nomGenere.append("sans bounty");
        }

        nomFormat.setValeur(nomGenere.toString());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // bouton créer : on vérifie que les valeurs sont bonnes, on appelle le controleur qui gère le reste
        if (e.getSource() == creerFormat) {
            // on controle les valeurs
            if (buyInMin.getValeurSlider() > buyInMax.getValeurSlider()) {
                messageErreur("Le buy-in maximum ne peut être inférieur au buy in minimum");
                return;
            }

            formCreationFormat.setNomFormat(nomFormat.getValeur());

            formCreationFormat.setNombreJoueurs(nombreJoueurs.getValeurSlider());

            formCreationFormat.setMinBuyIn(buyInMin.getValeurSlider());
            formCreationFormat.setMaxBuyIn(buyInMax.getValeurSlider());

            if (formCreationFormat.aAnte()) {
                if (anteMin.getValeurSlider() > anteMax.getValeurSlider()) {
                    messageErreur("L'ante maximum ne peut être inférieure à l'ante minimum");
                    return;
                }

                formCreationFormat.setMinAnte(anteMin.getValeurSlider());
                formCreationFormat.setMaxAnte(anteMax.getValeurSlider());
            }

            if (formCreationFormat.aRake()) {
                if (rakeMin.getValeurSlider() > rakeMax.getValeurSlider()) {
                    messageErreur("Le rake maximum ne peut être inférieure au rake minimum");
                    return;
                }

                formCreationFormat.setMinRake(rakeMin.getValeurSlider());
                formCreationFormat.setMaxRake(rakeMax.getValeurSlider());
            }

            if (formCreationFormat.bountyExiste()) {
                formCreationFormat.setBounty(bounty.estCoche());
            }

            fenetreFormat.creationFormat(formCreationFormat.getFormat());
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            deroulerChoix((String) e.getItem());
        }
    }
}
