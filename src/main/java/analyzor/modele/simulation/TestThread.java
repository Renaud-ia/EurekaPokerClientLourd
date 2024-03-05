package analyzor.modele.simulation;

public class TestThread extends Thread {
    private Integer valeurAcalculer;
    private int nombreCalculs = 0;
    public void run() {
        while (true) {
            try {
                verifierTaches();
                faireCalcul();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void verifierTaches() throws InterruptedException {
        while (valeurAcalculer == null || nombreCalculs > 10) {
            wait();
        }
    }

    private void faireCalcul() throws InterruptedException {
        Thread.sleep(100);
        System.out.println(valeurAcalculer);
        System.out.println(Math.sqrt(valeurAcalculer));
        nombreCalculs++;
    }

    public synchronized void valeurCalcul(int valeurAcalculer) {
        this.valeurAcalculer = valeurAcalculer;
        this.nombreCalculs = 0;
        notify();
    }


    public static void main(String[] args) throws InterruptedException {
        TestThread thread = new TestThread();
        thread.start();

        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            thread.valeurCalcul(i);
            long endTime = System.currentTimeMillis();
            System.out.println("Temps attente : " + (endTime - startTime));
            Thread.sleep(300);
        }

        Thread.sleep(5000);

        for (int i = 0; i < 10; i++) {
            thread.valeurCalcul(i);
            Thread.sleep(300);
        }
    }
}
