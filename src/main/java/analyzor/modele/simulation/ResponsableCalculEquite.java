package analyzor.modele.simulation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeReelle;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.vue.reutilisables.NombreModifiable;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * classe responsable du calcul d'équité
 * maintient constamment un thread de calcul en vie
 */
public class ResponsableCalculEquite extends Thread {
    private static final ConfigCalculatrice configCalculatrice;
    static {
        configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
    }
    private final CalculatriceEquite calculatriceEquite;
    private ComboIso comboIso;
    private NombreModifiable nombreModifiable;
    private List<RangeReelle> rangesVillains;
    private final Lock lockHashMap;
    protected final HashMap<ComboIso, Float> equitesDejaCalculees;
    protected final HashMap<ComboIso, Integer> nombreIterations;
    public ResponsableCalculEquite() {
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        lockHashMap = new ReentrantLock();
        equitesDejaCalculees = new HashMap<>();
        nombreIterations = new HashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                verifierTaches();
                calculerEquite();
            }

            catch (InterruptedException interruptedException) {
                break;
            }

            catch (Exception e) {
                System.out.println("INTERROMPU");
                break;
            }
        }
    }

    private synchronized void verifierTaches() throws InterruptedException {
        while(rangesVillains == null || comboIso == null) {
            wait();
        }
    }

    private synchronized void calculerEquite() throws InterruptedException {
        System.out.println("Thread du calcul : " + Thread.currentThread());
        System.out.println("ON A PRIS LE LOCK POUR CALCUL");
        equitesDejaCalculees.putIfAbsent(comboIso, 0f);
        nombreIterations.putIfAbsent(comboIso, 0);

        if (nombreIterations.get(comboIso) >= 10) wait();

        float equiteCalculee = calculatriceEquite.equiteGlobaleMain(
                        comboIso.toCombosReels().getFirst(),
                        new Board(),
                        rangesVillains);

        float equiteMoyenne = (nombreIterations.get(comboIso)
                        * equitesDejaCalculees.get(comboIso)
                        + equiteCalculee)
                        / (nombreIterations.get(comboIso) + 1);

        equitesDejaCalculees.put(comboIso, equiteMoyenne);
        nombreIterations.put(comboIso, nombreIterations.get(comboIso) + 1);

        System.out.println("EQUITE CALCULEE : " + equiteMoyenne);
        System.out.println("NOMBRE ITERATIONS : " + nombreIterations.get(comboIso));

        if (nombreModifiable != null) {
            nombreModifiable.modifierNombre(equiteMoyenne);
        }
    }

    public void setElementModifiable(NombreModifiable elementModifiable) {
        this.nombreModifiable = elementModifiable;
    }

    public void setRangesVillains(List<RangeReelle> rangesVillains) {
        System.out.println("RANGES FIXEES");
        this.rangesVillains = rangesVillains;
        reset();
    }

    public synchronized void lancerCalcul(ComboIso comboIso) {
        this.comboIso = comboIso;
        notify();
    }

    /**
     * appelé
     */
    private void reset() {
        System.out.println("Thread du reset : " + Thread.currentThread());
        lockHashMap.lock();
        System.out.println("ON A PRIS LE LOCK POUR RESET");
        equitesDejaCalculees.clear();
        nombreIterations.clear();
        lockHashMap.unlock();
    }

    public static void main(String[] args) throws InterruptedException {
        ResponsableCalculEquite responsableCalculEquite = new ResponsableCalculEquite();
        responsableCalculEquite.start();
        List<RangeReelle> rangesVillains = new ArrayList<>();
        RangeReelle rangeReelle = new RangeReelle();
        rangeReelle.remplir();
        RangeReelle rangeReelle2 = new RangeReelle();
        rangeReelle2.remplir();
        rangesVillains.add(rangeReelle);
        rangesVillains.add(rangeReelle2);

        ComboIso comboIso = new ComboIso("53o");

        responsableCalculEquite.setRangesVillains(rangesVillains);
        responsableCalculEquite.lancerCalcul(comboIso);

        Thread.sleep(1000);
        responsableCalculEquite.lancerCalcul(comboIso);
        System.out.println("ON A RELANCE CALCUL EN DOUBLON");

        Thread.sleep(1000);

        System.out.println("REAFFEACTATION RANGE");
        responsableCalculEquite.setRangesVillains(rangesVillains);
        System.out.println("APRES REAFFECTATION DANS MAIN");
        responsableCalculEquite.lancerCalcul(comboIso);
    }

}
