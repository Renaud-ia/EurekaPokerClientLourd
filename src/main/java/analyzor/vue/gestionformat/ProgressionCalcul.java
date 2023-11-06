package analyzor.vue.gestionformat;

import analyzor.controleur.WorkerAffichable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProgressionCalcul extends JPanel implements ActionListener {
    private final LigneCalcul ligneParente;
    private JProgressBar barreProgression;
    private JLabel textStatut;
    private JButton boutonStop;
    private JButton boutonOk;
    private WorkerAffichable workerActuel;
    protected ProgressionCalcul(LigneCalcul cadreParent) {
        this.ligneParente = cadreParent;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    protected void ajouterWorker(WorkerAffichable workerAffichable) {
        this.removeAll();
        workerActuel = workerAffichable;
        SwingUtilities.invokeLater(() -> {
            barreProgression = workerAffichable.getProgressBar();
            this.add(barreProgression);
            barreProgression.setStringPainted(true);
            barreProgression.setVisible(true);

            textStatut = workerAffichable.getLabelStatut();
            this.add(textStatut);

            boutonStop = new JButton("PAUSE");
            boutonStop.addActionListener(this);
            this.add(boutonStop);
        });

        workerAffichable.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName())) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    this.workerTermine();
                }
            }
        });

    }

    private void workerTermine() {
        System.out.println("worker terminé");
        this.remove(boutonStop);
        boutonOk = new JButton("OK");
        boutonOk.addActionListener(this);
        this.add(boutonOk);
        this.revalidate();
        this.repaint();
    }

    protected void lancerWorker() {
        workerActuel.execute();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonStop) {
            workerActuel.cancel(true);
        }
        else if (e.getSource() == boutonOk) {
            this.ligneParente.tacheTerminee();
        }
    }
}
