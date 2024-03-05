package analyzor.modele.simulation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeReelle;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.vue.reutilisables.NombreModifiable;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * classe responsable du calcul d'équité
 * maintient constamment un thread de gestion en vie
 */
public class ThreadCalculEquite extends Thread {
    private final static int MAX_ITERATIONS = 10;
    private static final ConfigCalculatrice configCalculatrice;
    static {
        configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
    }
    private final CalculatriceEquite calculatriceEquite;
    private ComboIso comboIso;
    private NombreModifiable nombreModifiable;
    private List<RangeReelle> rangesVillains;
    private final Lock lock;
    protected final HashMap<ComboIso, Float> equitesDejaCalculees;
    protected final HashMap<ComboIso, Integer> nombreIterations;
    private Thread threadReset;
    private Thread threadChangementCombo;
    private Thread threadCalcul;
    public ThreadCalculEquite() {
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        lock = new ReentrantLock();
        equitesDejaCalculees = new HashMap<>();
        nombreIterations = new HashMap<>();
        threadReset = null;
        threadChangementCombo = null;
        threadCalcul = null;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("NOUVEAU TOUR");
                estInitialise();

                if (threadReset != null && threadReset.isAlive()) {
                    interrompreCalcul();
                    threadReset.start();
                    threadReset.join();
                }

                else if (threadChangementCombo != null && threadChangementCombo.isAlive()) {
                    interrompreCalcul();
                    threadChangementCombo.start();
                    threadChangementCombo.join();
                }

                else {
                    System.out.println("CALCUL");
                    calculNecessaire();
                    if (threadCalcul != null) threadCalcul.join();
                    threadCalcul = new ThreadCalcul(comboIso);
                    threadCalcul.start();
                    System.out.println("FIN CALCUL");
                }
            }

            catch (InterruptedException interruptedException) {
                terminerThreads();
                break;
            }
        }
    }

    // méthodes de gestion de l'attente

    private synchronized void estInitialise() throws InterruptedException {
        while(threadReset == null || comboIso == null) {
            System.out.println("EN ATTENTE D'INITIALISATION");
            System.out.println(rangesVillains);
            System.out.println(comboIso);
            wait();
        }
    }

    private synchronized void calculNecessaire() throws InterruptedException {
        while (nombreIterations != null && nombreIterations.get(comboIso) > MAX_ITERATIONS) {
            System.out.println("EN ATTENTE DE PROCHAINS CALCULS");
            wait();
        }
    }

    // méthodes de fin propre

    private void terminerThreads() {
        if (threadReset != null && threadReset.isAlive()) {
            threadReset.interrupt();
        }
        if (threadChangementCombo != null && threadChangementCombo.isAlive()) {
            threadChangementCombo.interrupt();
        }
        interrompreCalcul();
    }

    private void interrompreCalcul() {
        if (threadCalcul != null && threadCalcul.isAlive()) {
            threadCalcul.interrupt();
        }
    }


    // méthodes publiques de controle

    public void setElementModifiable(NombreModifiable elementModifiable) {
        this.nombreModifiable = elementModifiable;
    }

    public void setRangesVillains(List<RangeReelle> rangesVillains) {
        threadReset = new ThreadReset(rangesVillains);
    }

    public synchronized void lancerCalcul(ComboIso comboIso) {
        this.comboIso = comboIso;
        System.out.println("ON NOTIFIE DUN COMBO FOURNI");
        notify();
    }


    private class ThreadReset extends Thread {
        private final List<RangeReelle> rangesProvisoires;
        private ThreadReset(List<RangeReelle> rangesVillains) {
            this.rangesProvisoires = rangesVillains;
        }

        @Override
        public void run() {
            lock.lock();
            rangesVillains = rangesProvisoires;
            System.out.println("ON A PRIS LE LOCK POUR RESET");
            equitesDejaCalculees.clear();
            nombreIterations.clear();
            lock.unlock();
        }
    }


    private class ThreadCalcul extends Thread {
        private final ComboIso comboCalcule;
        private ThreadCalcul(ComboIso comboIso) {
            comboCalcule = comboIso.copie();
        }

        @Override
        public void run() {
            System.out.println("EN ATTENTE DU LOCK");
            lock.lock();
            System.out.println("ON A PRIS LE LOCK POUR CALCUL");
            equitesDejaCalculees.putIfAbsent(comboCalcule, 0f);
            nombreIterations.putIfAbsent(comboCalcule, 0);

            float equiteCalculee = calculatriceEquite.equiteGlobaleMain(
                    comboCalcule.toCombosReels().getFirst(),
                    new Board(),
                    rangesVillains);

            float equiteMoyenne = (nombreIterations.get(comboCalcule)
                    * equitesDejaCalculees.get(comboCalcule)
                    + equiteCalculee)
                    / (nombreIterations.get(comboCalcule) + 1);

            equitesDejaCalculees.put(comboCalcule, equiteMoyenne);
            nombreIterations.put(comboCalcule, nombreIterations.get(comboCalcule) + 1);

            System.out.println("EQUITE CALCULEE : " + equiteMoyenne);
            System.out.println("NOMBRE ITERATIONS : " + nombreIterations.get(comboCalcule));

            if (nombreModifiable != null) {
                nombreModifiable.modifierNombre(equiteMoyenne);
            }
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadCalculEquite threadCalculEquite  = new ThreadCalculEquite();
        threadCalculEquite.start();
        List<RangeReelle> rangesVillains = new ArrayList<>();
        RangeReelle rangeReelle = new RangeReelle();
        rangeReelle.remplir();
        RangeReelle rangeReelle2 = new RangeReelle();
        rangeReelle2.remplir();
        rangesVillains.add(rangeReelle);
        rangesVillains.add(rangeReelle2);

        ComboIso comboIso = new ComboIso("53o");

        threadCalculEquite.setRangesVillains(rangesVillains);
        threadCalculEquite.lancerCalcul(comboIso);
        System.out.println("CALCUL LANCE");

        Thread.sleep(10000);
        threadCalculEquite.lancerCalcul(comboIso);
        System.out.println("ON A RELANCE CALCUL EN DOUBLON");

        Thread.sleep(10000);

        System.out.println("REAFFEACTATION RANGE");
        threadCalculEquite.setRangesVillains(rangesVillains);
        System.out.println("APRES REAFFECTATION DANS MAIN");
        threadCalculEquite.lancerCalcul(comboIso);
    }

}
