package analyzor.controleur.workers;

import analyzor.controleur.workers.WorkerAffichable;

public class WorkerTest extends WorkerAffichable {

    public WorkerTest(String nomTache, int nombreOperations) {
        super(nomTache, nombreOperations);
    }

    @Override
    protected Void executerTache() {
        try {
            for (int i = 0; i <= nombreOperations; i++) {
                Thread.sleep(10);
                publish(i);
            }
            tacheTerminee();
        }
        catch (Exception e) {
            System.out.println("Tâche interrompue");
            gestionInterruption();
        }
        return null;
    }
}
