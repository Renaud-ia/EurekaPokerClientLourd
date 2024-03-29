package analyzor.modele.equilibrage;

import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ProbaFold {
    private static final float PCT_NOT_FOLDED = 0.5f;

    private static final float PCT_FOLDED = 0.5f;
    private final int pas;
    public ProbaFold(int pas) {
        this.pas = pas;
    }

    public void estimerProbaFold(int nSituations, float pctFold, List<ComboIsole> combos) {
        if (pctFold < 0.1f) return;
        mainsJamaisFoldees(pctFold, combos);
        mainsToujoursFoldees(pctFold, combos);
    }


    private void mainsJamaisFoldees(float pctFold, List<ComboIsole> combos) {


        float pRangeAjoutee = 0;
        float notFolded = (1 - pctFold) * PCT_NOT_FOLDED;

        for (ComboIsole comboNoeud : combos) {
            boolean nonFolde = pRangeAjoutee < notFolded;


            if (nonFolde) {
                comboNoeud.setProbabiliteFoldEquite(probaNonFolde());
                comboNoeud.nestPasFolde();
            }
            else {
                comboNoeud.setProbabiliteFoldEquite(probaIndefinie());
            }

            pRangeAjoutee += comboNoeud.getPCombo();
        }
    }

    private void mainsToujoursFoldees(float pctFold, List<ComboIsole> combos) {


        float pRangeAjoutee = 0;
        float notFolded = pctFold * PCT_FOLDED;


        for (int i = combos.size() - 1; i >= 0; i--) {
            ComboIsole comboNoeud = combos.get(i);

            pRangeAjoutee += comboNoeud.getPCombo();

            boolean foldee = pRangeAjoutee < notFolded;


            if (foldee) {
                comboNoeud.setProbabiliteFoldEquite(probaPenaliteFold());
            }


        }
    }


    private float[] probaPenaliteFold() {
        float[] probaPenaliteFold = new float[nCategories()];
        float probaFoldCentPourcent = 0.5f;
        Arrays.fill(probaPenaliteFold, (1 - probaFoldCentPourcent) / (nCategories() - 1));
        probaPenaliteFold[probaPenaliteFold.length - 1] = probaFoldCentPourcent;

        return probaPenaliteFold;
    }

    private float[] probaToujoursFold() {
        float[] toujoursFolde = new float[nCategories()];
        Arrays.fill(toujoursFolde, 0);
        toujoursFolde[toujoursFolde.length - 1] = 1;

        return toujoursFolde;
    }

    private float[] probaNonFolde() {
        float[] nonFolde = new float[nCategories()];
        Arrays.fill(nonFolde, 0);
        nonFolde[0] = 1;

        return nonFolde;
    }

    private float[] probaIndefinie() {
        float[] indefini = new float[nCategories()];
        Arrays.fill(indefini, (float) 1 / nCategories());

        return indefini;
    }

    private int nCategories() {
        return (int) Math.ceil((double) 100 / pas) + 1;
    }
}
