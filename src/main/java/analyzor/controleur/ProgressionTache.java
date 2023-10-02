package analyzor.controleur;

import analyzor.vue.vues.VueBarreProgression;

import java.time.Duration;

public class ProgressionTache extends Thread {
    private final int nombreOperations;
    private int operationsTerminees;
    private boolean tacheActive;
    private String message;
    private Duration dureePrevue;
    private VueBarreProgression vueBarreProgression;
    public ProgressionTache(int nombreOperations, Duration dureePrevue) {
        this.nombreOperations = nombreOperations;
        this.dureePrevue = dureePrevue;
        this.operationsTerminees = 0;
        this.tacheActive = true;
    }
    public void run() {
        vueBarreProgression = new VueBarreProgression(nombreOperations, dureePrevue, message, this);
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
