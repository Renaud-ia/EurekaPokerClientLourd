package analyzor.modele.extraction;

import analyzor.controleur.workers.WorkerAffichable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerImportation extends WorkerAffichable {
    private final List<LecteurPartie> nouveauxFichiers;

    public WorkerImportation(String nomTache) {
        super(nomTache);
        this.nouveauxFichiers = new ArrayList<>();
        nombreOperations = 0;
    }

    public void ajouterLecteurs(List<LecteurPartie> lecteurParties) {
        nouveauxFichiers.addAll(lecteurParties);
        this.nombreOperations += lecteurParties.size();
        progressBar.setMaximum(nombreOperations);

        if (this.nouveauxFichiers.isEmpty()) {
            progressBar.setString("Aucun nouveau fichier");
        }
        else {
            progressBar.setString("En attente");
        }
    }

    @Override
    protected Void executerTache() {
        int i = 0;
        System.out.println(SwingUtilities.isEventDispatchThread());
        for (LecteurPartie lecteurPartie : nouveauxFichiers) {
            if (isCancelled()) {
                this.cancel(true);
                gestionInterruption();
                //on veut quand même ajouter le nombre de mains
                break;
            }
            try {
                lecteurPartie.sauvegarderPartie();
                publish(++i);
            }

            catch (Exception e) {
                //log pas sensible
                //on continue le traitement
                gestionInterruption();
                //on veut quand même ajouter le nombre de mains
                break;
                //todo : on pourrait capturer les exceptions ici, continuer le traitement sauf si trop d'erreurs
                // todo affiche un message de succès ....
            }
        }

        return null;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int progressValue = chunks.getLast();
        progressBar.setValue(progressValue);
        progressBar.setString(progressValue + "/" + nombreOperations + " fichiers traités");
    }
}
