package analyzor.modele.equilibrage;

import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * va estimer les probabilités de fold liées aux équités relatives
 * permet de corriger les erreurs dûes aux observations
 * pour l'instant va juste pas folder le top de range
 * todo : on pourrait améliorer
 */
public class ProbaFold {
    private final static Logger logger = LogManager.getLogger(ProbaFold.class);
    private static final float PCT_NOT_FOLDED = 0.5f;
    // todo à revoir surement trop elevé ou bien faire qu'on augmente la proba de fold sans l'imposer non plus
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

    /**
     * va attribuer une probabilité de fold nulle aux mains trop fortes en termes d'équité
     */
    private void mainsJamaisFoldees(float pctFold, List<ComboIsole> combos) {
        // on crée simplement un noeud par combo
        // on calcule les probas
        float pRangeAjoutee = 0;
        float notFolded = (1 - pctFold) * PCT_NOT_FOLDED;

        for (ComboIsole comboNoeud : combos) {
            boolean nonFolde = pRangeAjoutee < notFolded;
            // la liste va garder le type d'origine
            logger.trace(comboNoeud + " sera foldé : " + !nonFolde);
            // les combos sont triés par ordre d'équité en amont
            if (nonFolde) {
                comboNoeud.setProbabiliteFoldEquite(probaNonFolde());
            }
            else {
                comboNoeud.setProbabiliteFoldEquite(probaIndefinie());
            }

            pRangeAjoutee += comboNoeud.getPCombo();
        }
    }

    private void mainsToujoursFoldees(float pctFold, List<ComboIsole> combos) {
        // on crée simplement un noeud par combo
        // on calcule les probas
        float pRangeAjoutee = 0;
        float notFolded = pctFold * PCT_FOLDED;

        // on parcout en sens inverse
        for (int i = combos.size() - 1; i >= 0; i--) {
            ComboIsole comboNoeud = combos.get(i);
            // on rajoute ça avant pour éviter effet de seuil si fold trop faible
            pRangeAjoutee += comboNoeud.getPCombo();

            boolean foldee = pRangeAjoutee < notFolded;
            // la liste va garder le type d'origine
            logger.trace(comboNoeud + " sera toujours foldé : " + foldee);
            // les combos sont triés par ordre d'équité en amont
            if (foldee) {
                comboNoeud.setProbabiliteFoldEquite(probaPenaliteFold());
            }

            // pas besoin de proba indéfinie car déjà définie avant
        }
    }

    /**
     * @return une pénalité de fold qui n'est pas 100%
     */
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
