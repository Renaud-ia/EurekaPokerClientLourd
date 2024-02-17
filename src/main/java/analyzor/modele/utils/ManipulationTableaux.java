package analyzor.modele.utils;

/**
 * méthodes utilitaires sur les tableaux
 */
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

    /**
     * renvoie une copie profonde d'un tableau 2D (clone ne marche que pour 1D)
     */
    public static float[][] copierTableau(float[][] tableauOriginal) {
        // Création d'un nouveau tableau pour la copie
        float[][] tableauClone = new float[tableauOriginal.length][];
        for (int i = 0; i < tableauOriginal.length; i++) {
            tableauClone[i] = new float[tableauOriginal[i].length];
            System.arraycopy(tableauOriginal[i], 0, tableauClone[i], 0, tableauOriginal[i].length);
        }

        return tableauClone;
    }
}
