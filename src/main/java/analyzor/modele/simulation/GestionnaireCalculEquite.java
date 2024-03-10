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

public class GestionnaireCalculEquite {
    private final static int MAX_ITERATIONS = 50;
    private static final ConfigCalculatrice configCalculatrice;
    static {
        configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
    }
    private final CalculatriceEquite calculatriceEquite;
    private List<RangeReelle> rangesVillains;
    private ComboIso comboCalcule;
    private NombreModifiable nombreModifiable;
    protected final HashMap<ComboIso, Float> equitesDejaCalculees;
    protected final HashMap<ComboIso, Integer> nombreIterations;
    private ThreadCalcul calculActuel;
    private Thread threadLancementCalcul;

    public GestionnaireCalculEquite() {
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        this.equitesDejaCalculees = new HashMap<>();
        this.nombreIterations = new HashMap<>();
    }

    public void setRangesVillains(List<RangeReelle> nouvellesRanges) {
        finirThreadLancement();

        // on crée un thread pour éviter de faire patienter le thread main pendant qu'on attend
        threadLancementCalcul = new Thread() {
            @Override
            public void run() {
                if (calculActuel != null && calculActuel.isAlive()) {
                    calculActuel.interrupt();
                    try {
                        calculActuel.join();
                    }
                    catch (InterruptedException ignored) {
                    }
                }
                rangesVillains = nouvellesRanges;
            }

        };

        threadLancementCalcul.start();
    }

    public void lancerCalcul(NombreModifiable elementModifiable, ComboIso comboIso) {
        finirThreadLancement();

        // on crée un thread pour éviter de faire patienter le thread main pendant qu'on attend
        threadLancementCalcul = new Thread() {
            @Override
            public void run() {
                if (calculActuel != null && calculActuel.isAlive()) {
                    calculActuel.interrupt();
                    try {
                        calculActuel.join();
                    }
                    catch (InterruptedException ignored) {
                        return;
                    }
                }
                equitesDejaCalculees.clear();
                nombreIterations.clear();
                nombreModifiable = elementModifiable;
                comboCalcule = comboIso;
                calculActuel = new ThreadCalcul();
                calculActuel.start();
            }

        };

        threadLancementCalcul.start();
    }

    private void finirThreadLancement() {
        // todo OPTIMISATION distinguer thread lancement calcul (qu'on peut interrompre) et thread changement ranges

        // chaque fois qu'on relance, on attend que le thread de modification soit fini
        if (threadLancementCalcul != null && threadLancementCalcul.isAlive()) {
            try {
                threadLancementCalcul.join();
            }
            catch (InterruptedException e) {
                throw new RuntimeException("Le thread appelant a été interrompu");
            }
        }
    }

    private class ThreadCalcul extends Thread {
        private ThreadCalcul() {
        }
        @Override
        public void run() {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                if (Thread.currentThread().isInterrupted()) return;
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

                if (Thread.currentThread().isInterrupted()) return;

                if (nombreModifiable != null) {
                    nombreModifiable.modifierNombre(equiteMoyenne);
                }
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        GestionnaireCalculEquite threadCalculEquiteV2 = new GestionnaireCalculEquite();

        List<RangeReelle> rangesVillains = new ArrayList<>();
        RangeReelle rangeReelle = new RangeReelle();
        rangeReelle.remplir();
        rangesVillains.add(rangeReelle);

        threadCalculEquiteV2.setRangesVillains(rangesVillains);

        ComboIso comboIso = new ComboIso("53o");
        threadCalculEquiteV2.lancerCalcul(null, comboIso);

        System.out.println("#####LE PROGRAMME CONTINUE V1#####");

        Thread.sleep(5000);

        long startTime = System.currentTimeMillis();
        threadCalculEquiteV2.lancerCalcul(null, comboIso);
        long endTime = System.currentTimeMillis();
        System.out.println("TEMPS ATTENTE PROCESSUS CHANGEMENT COMBO : " + (endTime - startTime));

        System.out.println("#####LE PROGRAMME CONTINUE V2#####");

        threadCalculEquiteV2.setRangesVillains(rangesVillains);

        System.out.println("RANGES INITIALISEES MAIS CALCUL NON LANCEE");

        Thread.sleep(10000);

        ComboIso comboIso2 = new ComboIso("AKs");
        System.out.println("#####LE PROGRAMME CONTINUE V3#####");
        threadCalculEquiteV2.lancerCalcul(null, comboIso2);
    }
}
