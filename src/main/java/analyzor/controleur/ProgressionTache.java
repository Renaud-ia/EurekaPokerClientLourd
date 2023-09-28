package analyzor.controleur;

public class ProgressionTache {
    private final int nombreOperations;
    private int operationsTerminees;
    private boolean tacheActive;
    private String message;
    public ProgressionTache(int nombreOperations) {
        this.nombreOperations = nombreOperations;
        this.operationsTerminees = 0;
        this.tacheActive = true;
    }
    public void operationTerminee() {
        operationsTerminees++;
    }

    public int avancement() {
        return operationsTerminees;
    }

    public int getTotalOperations() {
        return nombreOperations;
    }

    public void interrompreTache() {
        tacheActive = false;
    }

    public boolean isTacheActive() {
        return tacheActive;
    }

    public void ajouterMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
