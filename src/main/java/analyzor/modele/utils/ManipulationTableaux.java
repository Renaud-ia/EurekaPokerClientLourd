package analyzor.modele.utils;

public class ManipulationTableaux {
    public static float[] aplatir(float[][] tableau2D) {
        // Calcul de la taille du tableau à une dimension
        int taille = 0;
        for (float[] ligne : tableau2D) {
            taille += ligne.length;
        }

        // Initialisation du tableau à une dimension
        float[] tableau1D = new float[taille];

        // Remplissage du tableau à une dimension en mettant les chiffres à la suite
        int index = 0;
        for (float[] ligne : tableau2D) {
            for (float nombre : ligne) {
                tableau1D[index++] = nombre;
            }
        }

        return tableau1D;
    }
}
