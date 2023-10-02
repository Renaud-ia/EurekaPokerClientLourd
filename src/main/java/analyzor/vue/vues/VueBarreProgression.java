package analyzor.vue.vues;

import analyzor.controleur.ProgressionTache;
import analyzor.vue.composants.CadreLarge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;

public class VueBarreProgression extends JDialog implements ActionListener {
    private JProgressBar barreProgression;
    private ProgressionTache tache;
    private final int nombreOperations;
    private final JLabel message;
    private final JButton boutonDemarrer = new JButton("Demarrer");
    private final JButton boutonStop = new JButton("Stop");
    private boolean actif = false;

    public VueBarreProgression(int nombreOperations, Duration dureePrevue, String message, ProgressionTache tache) {
        super((JFrame) null, "Gestion de la room", false);
        this.nombreOperations = nombreOperations;
        this.message = new JLabel(message);

        setAlwaysOnTop(true);
        configurerBoutons();
        desactiverFermer();
        construireFenetre();
    }

    private void configurerBoutons() {
        boutonDemarrer.addActionListener(this);
        boutonStop.addActionListener(this);
        boutonStop.setEnabled(false);
    }

    private void construireFenetre() {
        setLayout(new BorderLayout());

        CadreLarge panneauMessage = new CadreLarge();
        panneauMessage.add(message);
        add(panneauMessage);

        CadreLarge panneauBarre = new CadreLarge();
        barreProgression = new JProgressBar();
        barreProgression.setMinimum(0);
        barreProgression.setMaximum(nombreOperations);
        barreProgression.setStringPainted(true);
        panneauBarre.add(barreProgression);
        this.add(panneauBarre);

        CadreLarge panneauBoutons = new CadreLarge();
        panneauBoutons.add(boutonDemarrer);
        panneauBoutons.add(boutonStop);
        this.add(panneauBoutons);
    }

    public void valeurBarre(int valeur) {
        barreProgression.setValue(valeur);
    }

    public void desactiverFermer() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public void activerFermer() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.boutonStop) {
            this.actif = false;
            boutonStop.setEnabled(false);

            CadreLarge cadreWarning = new CadreLarge();
            JLabel labelStop = new JLabel("Veuillez patienter quelques instants");
            labelStop.setForeground(Color.RED);
            cadreWarning.add(labelStop);
            this.add(cadreWarning);
            this.pack();

        }
        if (e.getSource() == this.boutonDemarrer) {
            this.actif = true;
            boutonDemarrer.setEnabled(false);
            boutonStop.setEnabled(true);
        }
    }
}
