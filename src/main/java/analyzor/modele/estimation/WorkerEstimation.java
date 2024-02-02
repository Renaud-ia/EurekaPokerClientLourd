package analyzor.modele.estimation;

import analyzor.controleur.WorkerAffichable;
import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.TourMain;

public class WorkerEstimation extends WorkerAffichable {
    private final FormatSolution formatSolution;
    public WorkerEstimation(FormatSolution formatSolution) {
        super("Calcul");
        this.formatSolution = formatSolution;
    }

    @Override
    protected Void executerTache() throws Exception {
        ProfilJoueur profilVillain = ObjetUnique.selectionnerVillain();
        Estimateur estimateur = new Estimateur(formatSolution, profilVillain);
        Integer nSituations = estimateur.setRound(TourMain.Round.PREFLOP);
        if (nSituations == null) return null;

        progressBar.setMaximum(nSituations);

        try {
            System.out.println("THREAD ACTUEL DANS WORKER " + Thread.currentThread());
            int i = 0;
            while (estimateur.calculerRangeSuivante()) {
                if (isCancelled()) {
                    gestionInterruption();
                    break;
                }
                this.publish(++i);
            }
        }
        catch (Exception e) {
            gestionInterruption();
        }

        return null;
    }

    /**
     * méthode spéciale d'annulation qui assure qu'on ne va jamais annuler quand requete bdd
     */
    public void annulerTache(boolean mayInterruptIfRunning) {
        if (!mayInterruptIfRunning) {
            this.cancel(false);
            return;
        }

        ConnexionBDD.empecherConnexion();
        while(ConnexionBDD.connexionActive()) {
            try {
                System.out.println("CONNEXION ACTIVE");
                Thread.sleep(10);
            }
            catch (InterruptedException e) {
                break;
            }
        }
        ConnexionBDD.retablirConnexion();
        this.cancel(true);
    }
}
