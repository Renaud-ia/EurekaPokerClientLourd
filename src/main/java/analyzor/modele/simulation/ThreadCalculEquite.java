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

/**
 * classe qui gère le thread de fond pour le calcul d'équité
 * reste en pause tant que des ranges + combo n'ont pas été saisies
 * valeurs peuvent être changées à tout moment de manière safe
 * ne change pas la valeur du combo si des valeurs sont changées pendant le calcul
 */
public class ThreadCalculEquite extends Thread {
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
    private boolean enAttente;
    private boolean calculTermine;
    private boolean comboChange;
    protected final HashMap<ComboIso, Float> equitesDejaCalculees;
    protected final HashMap<ComboIso, Integer> nombreIterations;
    public ThreadCalculEquite() {
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        equitesDejaCalculees = new HashMap<>();
        nombreIterations = new HashMap<>();
        enAttente = true;
        calculTermine = false;
        comboChange = false;
    }

    @Override
    public void run() {
        while(true) {
            try {
                miseEnAttente();
                calculerEquite();
            }

            catch (InterruptedException e) {
                break;
            }
        }
    }

    private synchronized void miseEnAttente() throws InterruptedException {
        while (enAttente || calculTermine) {
            System.out.println("ON EST EN ATTENTE");
            wait();
        }
    }

    private void calculerEquite() throws InterruptedException {
        System.out.println("TOUR CALCUL EQUITE");
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
        if (nombreIterations.get(comboCalcule) >= MAX_ITERATIONS) {
            calculTermine = true;
        }

        // conditions qui servent à éviter d'inscrire un résultat si les valeurs ont changé entre temps
        if (nouveauCombo != null) {
            System.out.println("NOUVEAU COMBO DETECTE : " + nouveauCombo);
            comboCalcule = nouveauCombo;
            nouveauCombo = null;
            return;
        }

        if (nouvellesRanges != null) {
            rangesVillains = nouvellesRanges;
            nouvellesRanges = null;
            System.out.println("RANGES REINITIALISEES");
            return;
        }

        if (comboChange) {
            equitesDejaCalculees.clear();
            nombreIterations.clear();
            comboChange = false;
            return;
        }

        System.out.println("EQUITE DEJA CALCULEE : " + equitesDejaCalculees.get(comboCalcule));
        System.out.println("EQUITE AJOUTEE : " + equiteCalculee);
        System.out.println("ITERATIONS : " + nombreIterations.get(comboCalcule));
        System.out.println("EQUITE MOYENNE : " + equiteMoyenne);


        if (nombreModifiable != null) {
            nombreModifiable.modifierNombre(equiteMoyenne);
        }
    }

    public void setRangesVillains(List<RangeReelle> rangesVillains) {
        // initialisation
        if (this.rangesVillains == null) {
            this.rangesVillains = rangesVillains;
        }

        // ou changement de ranges
        else {
            nouvellesRanges = rangesVillains;
        }

        enAttente = true;
    }

    public synchronized void lancerCalcul(NombreModifiable elementModifiable, ComboIso comboIso) {
        this.nombreModifiable = elementModifiable;
        if (this.comboCalcule == null) {
            this.comboCalcule = comboIso;
        }
        else this.nouveauCombo = comboIso;

        enAttente = false;
        calculTermine = false;
        comboChange = true;

        notify();
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadCalculEquite threadCalculEquiteV2 = new ThreadCalculEquite();
        threadCalculEquiteV2.start();

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
