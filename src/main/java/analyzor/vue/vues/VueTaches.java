package analyzor.vue.vues;

import analyzor.controleur.ControleurPrincipal;
import analyzor.controleur.WorkerAffichable;
import analyzor.vue.composants.CadreLarge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VueTaches extends JFrame implements ActionListener {
    /*
    gère l'affichage des tâches lourdes en background
     */
    private final ControleurPrincipal controleur;
    private final List<WorkerAffichable> workers = new ArrayList<>();
    private final JButton boutonLancer = new JButton("Lancer");
    private final JButton boutonArreter = new JButton("Stop");
    private final JPanel panneauContenu = new JPanel();
    private boolean enCours = false;
    private int indexWorker = 0;
    public VueTaches(ControleurPrincipal controleur) {
        super();
        this.controleur = controleur;
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));
        this.add(panneauContenu);
        ajouterControles();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fermetureFenetre();
            }
        });
    }

    public void afficher() {
        setVisible(true);
    }

    private void ajouterControles() {
        CadreLarge cadreControles = new CadreLarge();
        cadreControles.setLayout(new FlowLayout());

        boutonLancer.addActionListener(this);
        boutonArreter.addActionListener(this);
        boutonArreter.setEnabled(false);
        cadreControles.add(boutonLancer);
        cadreControles.add(boutonArreter);

        panneauContenu.add(cadreControles);

    }

    public boolean ajouterWorker(WorkerAffichable worker) {
        // pour la première fois, on ne laisse pas plus d'un worker
        if (workers.size() > 0) return false;
        System.out.println("Worker ajjouté");
        workers.add(worker);
        int indexWorker = workers.indexOf(worker);
        afficherWorker(worker, indexWorker);
        panneauContenu.validate();
        panneauContenu.repaint();
        this.revalidate();
        this.pack();
        return true;
    }

    private void afficherWorker(WorkerAffichable worker, int indexWorker) {
        SwingUtilities.invokeLater(() -> {
            CadreLarge cadreWorker = new CadreLarge();
            cadreWorker.setLayout(new FlowLayout());
            cadreWorker.add(worker.getNomWorker());
            JProgressBar progressBar = worker.getProgressBar();
            progressBar.setStringPainted(true);
            progressBar.setVisible(true);
            cadreWorker.add(progressBar);
            cadreWorker.add(worker.getLabelStatut());

            //todo il faudra gérer ça plus tard
            //JButton boutonSupprimer = new JButton("Supprimer");
            //boutonSupprimer.setActionCommand(String.valueOf(indexWorker));
            //boutonSupprimer.addActionListener(this);
            //cadreWorker.add(boutonSupprimer);

            panneauContenu.add(cadreWorker);
        });
    }

    public void lancerTaches() {
        /*
        désactive la fermeture
        on désactive bouton supprimer et lancer, on active stop
        dit au controleur de désactiver les vues
        lance les tâches une par une
         */
        enCours = true;
        controleur.desactiverVues();
        boutonLancer.setEnabled(false);
        boutonArreter.setEnabled(true);

        lancerWorker();
    }

    private void lancerWorker() {
        if (indexWorker + 1 > workers.size()) {
            terminerTache();
            return;
        }
        WorkerAffichable workerLance = workers.get(indexWorker);
        if (workerLance.isCancelled()) {
            indexWorker++;
            lancerWorker();
        }
        workerLance.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName())) {
                if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    if (workerLance.isCancelled()) {
                        terminerTache();
                    }
                    else {
                        indexWorker++;
                        lancerWorker();
                    }
                }
            }
        });
        if (enCours) workerLance.execute();
    }

    private void terminerTache() {
        /*
        à la fin, on réactive les boutons adaptés
        */
        enCours = false;
        boutonLancer.setEnabled(true);
        boutonArreter.setEnabled(false);
        indexWorker = 0;
    }

    private void fermetureFenetre() {
        /*
        supprime les workers
        dit au controleur de réactiver les vues
         */
        if (enCours) return;
        workers.clear();
        this.panneauContenu.removeAll();
        controleur.reactiverVues();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonLancer) {
            lancerTaches();
        }
        else if (e.getSource() == boutonArreter) {
            WorkerAffichable worker = workers.get(indexWorker);
            worker.cancel(true);
        }
    }
}
