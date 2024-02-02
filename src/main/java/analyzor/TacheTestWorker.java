package analyzor;

public class TacheTestWorker {
    private boolean interrompu;
    public TacheTestWorker() {
        interrompu = false;
    }

    public void faireQuelqueChose() {
        for (int i = 0; i < 10000000; i++) {
            try {
                if (interrompu) {
                    System.out.println("Thread interrompu");
                    System.out.println("DANS LA TACHE" + Thread.currentThread());
                    break;
                }
                System.out.println(i);
            }
            catch (Exception e) {
                System.out.println("EXCEPTION : " + e);
                break;
            }

        }
    }

    public void interrompre() {
        System.out.println("SIGNAL INTERRUPTION");
        interrompu = true;
    }
}
