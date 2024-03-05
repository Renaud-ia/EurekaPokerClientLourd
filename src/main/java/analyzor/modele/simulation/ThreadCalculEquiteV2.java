package analyzor.modele.simulation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeReelle;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.vue.reutilisables.NombreModifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThreadCalculEquiteV2 extends Thread {
    private final static int MAX_ITERATIONS = 10;
    private static final ConfigCalculatrice configCalculatrice;
    static {
        configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
    }
    private final CalculatriceEquite calculatriceEquite;
    private NombreModifiable nombreModifiable;
    private List<RangeReelle> rangesVillains;
    private List<RangeReelle> nouvellesRanges;
    private ComboIso comboCalcule;
    private ComboIso nouveauCombo;
    protected final HashMap<ComboIso, Float> equitesDejaCalculees;
    protected final HashMap<ComboIso, Integer> nombreIterations;
    public ThreadCalculEquiteV2() {
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        equitesDejaCalculees = new HashMap<>();
        nombreIterations = new HashMap<>();
    }

    @Override
    public void run() {
        while(true) {
            try {
                miseEnAttente();
                calculerEquite();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private synchronized void miseEnAttente() throws InterruptedException {
        while (!estInitialise() || calculTermine()) {
            System.out.println("ON EST EN ATTENTE");
            wait();
        }
    }

    private boolean calculTermine() {
        if (nombreIterations.get(comboCalcule) != null) {
            System.out.println("NOMBRE TOURS COMPLETES : " + (nombreIterations.get(comboCalcule)));
        }
        return (nombreIterations.get(comboCalcule) != null && nombreIterations.get(comboCalcule) >= MAX_ITERATIONS);
    }

    private boolean estInitialise() {
        System.out.println("INITIALISE : " + (rangesVillains != null && comboCalcule != null));
        return rangesVillains != null && comboCalcule != null;
    }

    private void calculerEquite() throws InterruptedException {
        System.out.println("TOUR CALCUL EQUITE");
        equitesDejaCalculees.putIfAbsent(comboCalcule, 0f);
        nombreIterations.putIfAbsent(comboCalcule, 0);
        float equiteCalculee = calculatriceEquite.equiteGlobaleMain(
                comboCalcule.toCombosReels().getFirst(),
                new Board(),
                rangesVillains);

        System.out.println("EQUITE DEJA CALCULEE : " + equitesDejaCalculees.get(comboCalcule));
        System.out.println("EQUITE AJOUTEE : " + equiteCalculee);
        System.out.println("ITERATIONS : " + nombreIterations.get(comboCalcule));

        float equiteMoyenne = (nombreIterations.get(comboCalcule)
                * equitesDejaCalculees.get(comboCalcule)
                + equiteCalculee)
                / (nombreIterations.get(comboCalcule) + 1);

        equitesDejaCalculees.put(comboCalcule, equiteMoyenne);
        nombreIterations.put(comboCalcule, nombreIterations.get(comboCalcule) + 1);

        if (nouveauCombo != null) {
            System.out.println("NOUVEAU COMBO DETECTE : " + nouveauCombo);
            comboCalcule = nouveauCombo;
            nouveauCombo = null;
            return;
        }

        if (nouvellesRanges != null) {
            rangesVillains = nouvellesRanges;
            nouvellesRanges = null;
            comboCalcule = null;
            equitesDejaCalculees.clear();
            nombreIterations.clear();
            System.out.println("RANGES REINITIALISEES");
            return;
        }

        System.out.println("EQUITE MOYENNE : " + equiteMoyenne);
        Thread.sleep(1000);
        //nombreModifiable.modifierNombre(equiteMoyenne);
    }

    public void setElementModifiable(NombreModifiable elementModifiable) {
        this.nombreModifiable = elementModifiable;
    }

    public void setRangesVillains(List<RangeReelle> rangesVillains) {
        if (this.rangesVillains == null) {
            synchronized (this) {
                this.rangesVillains = rangesVillains;
                notify();
            }
        }
        else {
            nouvellesRanges = rangesVillains;
        }
    }

    public synchronized void lancerCalcul(ComboIso comboIso) {
        if (this.comboCalcule == null) {
            this.comboCalcule = comboIso;
            notify();
        }
        else this.nouveauCombo = comboIso;
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadCalculEquiteV2 threadCalculEquiteV2 = new ThreadCalculEquiteV2();
        threadCalculEquiteV2.start();

        List<RangeReelle> rangesVillains = new ArrayList<>();
        RangeReelle rangeReelle = new RangeReelle();
        rangeReelle.remplir();
        rangesVillains.add(rangeReelle);

        threadCalculEquiteV2.setRangesVillains(rangesVillains);

        ComboIso comboIso = new ComboIso("53o");
        threadCalculEquiteV2.lancerCalcul(comboIso);

        System.out.println("#####LE PROGRAMME CONTINUE#####");

        Thread.sleep(4000);

        long startTime = System.currentTimeMillis();
        threadCalculEquiteV2.lancerCalcul(comboIso);
        long endTime = System.currentTimeMillis();
        System.out.println("TEMPS ATTENTE PROCESSUS CHANGEMENT COMBO : " + (endTime - startTime));

        System.out.println("#####LE PROGRAMME CONTINUE#####");

        threadCalculEquiteV2.setRangesVillains(rangesVillains);

        ComboIso comboIso2 = new ComboIso("AKs");

        Thread.sleep(4000);
        threadCalculEquiteV2.lancerCalcul(comboIso2);
    }
}
