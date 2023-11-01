package analyzor.controleur;

import javax.swing.*;

public class WorkerAffichable extends SwingWorker<Void, Integer> {
    private final JLabel nomWorker;
    private final JProgressBar progressBar;
    private final JLabel labelStatut;
    final int nombreOperations;

    public WorkerAffichable(String nomTache, int nombreOperations) {
        this.nomWorker = new JLabel(nomTache);
        this.nombreOperations = nombreOperations;
        this.progressBar = new JProgressBar();
        progressBar.setMaximum(nombreOperations);
        labelStatut = new JLabel("En cours");
    }

    // vérifier si isCancelled()
    //utiliser publih(int i) pour mettre une valeur à la barre de progression
    protected Void executerTache() throws Exception {
        //todo c'est dégueulasse -> comment faire dériver les classes ???
        return null;
    }

    protected void gestionInterruption() {
        // Traitement de l'interruption
        labelStatut.setText("Interrompu");
        progressBar.setValue(0); // Réinitialisation de la progress bar
        Thread.currentThread().interrupt(); // Rétablir le statut d'interruption
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

}
