package analyzor;

public class TestDiscretisation {
    public static void main(String[] args) {
        // Tableau de valeurs float entre 0 et 1
        float[] values = {0.12f, 0.28f, 0.46f, 0.75f, 0.89f};

        // Pas de discrétisation
        float step = 0.05f;

        // Initialisation des compteurs pour chaque intervalle
        int[] counts = new int[(int) Math.ceil(1 / step)];

        // Compter les valeurs dans chaque intervalle
        for (float value : values) {
            int index = (int) Math.floor(value / step);
            counts[index]++;
        }

        // Calculer le pourcentage de valeurs dans chaque intervalle
        int totalCount = values.length;
        float[] percentages = new float[counts.length];
        for (int i = 0; i < counts.length; i++) {
            percentages[i] = (float) counts[i] / totalCount * 100;
        }

        // Afficher les pourcentages
        System.out.println("Pourcentage de valeurs dans chaque intervalle :");
        for (int i = 0; i < percentages.length; i++) {
            float lowerBound = i * step;
            float upperBound = (i + 1) * step;
            System.out.printf("%.2f - %.2f : %.2f%%\n", lowerBound, upperBound, percentages[i]);
        }
    }
}
