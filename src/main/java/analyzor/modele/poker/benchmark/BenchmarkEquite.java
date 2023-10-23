package analyzor.modele.poker.benchmark;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeReelle;
import analyzor.modele.poker.evaluation.CalculatriceEquite;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkEquite {
    public static void main(String[] args) {
    CalculatriceEquite calculatrice = new CalculatriceEquite(
            200,
            200,
            400,
            0.4f,
            0.05f,
            5
    );

    List<Float> erreurs = new ArrayList<>();
    List<Float> temps = new ArrayList<>();

    for (GenerateurTestsEquite.TestEquite testEquite : new GenerateurTestsEquite().testsEquite) {
        long startTime = System.nanoTime();
        float equite = calculatrice.equiteGlobaleMain(testEquite.heroCombo, testEquite.board, testEquite.rangesVillains);
        long endTime = System.nanoTime();
        double dureeMS = (endTime - startTime) / 1_000_000.0;

        erreurs.add(Math.abs(equite - testEquite.result));
        temps.add((float) dureeMS);

        System.out.println("Combo teste : " + testEquite.heroCombo);
        System.out.println("Valeur calculée : " + equite);
        System.out.println("Référence : " + testEquite.result);
    }

    float erreurMoyenne = getMoyenne(erreurs);
    float ecartTypeErreur = getEcartType(erreurs, erreurMoyenne);

    float tempsMoyen = getMoyenne(temps);
    float ecartTypeTemps = getEcartType(temps, tempsMoyen);

    System.out.println("Erreur moyenne : " + erreurMoyenne);
    System.out.println("Ecart type erreur : " + ecartTypeErreur);

    System.out.println("Temps moyens : " + tempsMoyen);
    System.out.println("Ecart type temps : " + ecartTypeTemps);

    }

    private static float getEcartType(List<Float> erreurs, float erreurMoyenne) {
        float variance = (float) erreurs.stream()
                .mapToDouble(num -> (num - erreurMoyenne) * (num - erreurMoyenne))
                .sum() / erreurs.size();
        return (float) Math.sqrt(variance);
    }

    private static float getMoyenne(List<Float> numbers) {
        return (float) numbers.stream()
                .mapToDouble(num -> num)
                .average()
                .orElse(0.0f);
    }
}
