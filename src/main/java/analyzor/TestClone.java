package analyzor;

import java.util.Arrays;

public class TestClone {
    public static void main(String[] args) {
        int[] hypotheseActuelle = new int[] {-1, 0, 0};

        while ((hypotheseActuelle = prochaineHypothese(hypotheseActuelle, 3, 3)) != null) {
            System.out.println(Arrays.toString(hypotheseActuelle));
        }



    }

    private static int[] prochaineHypothese(int[] hypotheseActuelle, int nHypotheses, int maxHYp) {
        for (int iComposante = 0; iComposante < nHypotheses; iComposante++) {
            int prochaineHypothese = hypotheseActuelle[iComposante] + 1;

            // si l'hypothèse suivante existe
            if (prochaineHypothese < maxHYp) {
                hypotheseActuelle[iComposante]++;
                break;
            }
            // si on a tout parcouru on retourne null
            else if (iComposante == hypotheseActuelle.length -1) return null;

                //
            else {
                hypotheseActuelle[iComposante] = 0;
            }

        }

        return hypotheseActuelle;
    }

}
