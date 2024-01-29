package analyzor.controleur;

import javax.swing.*;

public abstract class WorkerAffichable extends SwingWorker<Void, Integer> {
    private final JLabel nomWorker;
    protected final JProgressBar progressBar;
    protected final JLabel labelStatut;
    protected int nombreOperations;
    private boolean annule;

    public WorkerAffichable(String nomTache, int nombreOperations) {
        this(nomTache);
        this.nombreOperations = nombreOperations;
        progressBar.setMaximum(nombreOperations);
    }

    public WorkerAffichable(String nomTache) {
        this.nomWorker = new JLabel(nomTache);
        this.progressBar = new JProgressBar();
        labelStatut = new JLabel("En cours");
        annule = false;
    }

    // vérifier si isCancelled()
    //utiliser publih(int i) pour mettre une valeur à la barre de progression
    protected abstract Void executerTache() throws Exception;

    protected void gestionInterruption() {
        // Traitement de l'interruption
        labelStatut.setText("Interrompu");
        progressBar.setValue(0);
        Thread.currentThread().interrupt();
    }

    protected void tacheTerminee() {
        labelStatut.setText("Termin\u00E9");
    }

    protected void afficherErreur(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    message + "\nContactez le support si le problème persiste",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    //on peut lancer une fenêtre dans done si on veut ajouter quelque chose à la fin

    @Override
    protected Void doInBackground() throws Exception {
        executerTache();
        return null;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int progressValue = chunks.get(chunks.size() - 1);
        progressBar.setValue(progressValue);
    }

    public JLabel getNomWorker() {
        return nomWorker;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getLabelStatut() {
        return labelStatut;
    }

    public void setStatut(String statut) {
        labelStatut.setText(statut);
    }

    public boolean estAnnule() {
        return annule;
    }

}
