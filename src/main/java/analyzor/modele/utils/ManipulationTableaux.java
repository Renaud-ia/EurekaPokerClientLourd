package analyzor.modele.utils;


public class ManipulationTableaux {
    public static float[] aplatir(float[][] tableau2D) {
        
        int taille = 0;
        for (float[] ligne : tableau2D) {
            taille += ligne.length;
        }

        
        float[] tableau1D = new float[taille];

        
        int index = 0;
        for (float[] ligne : tableau2D) {
            for (float nombre : ligne) {
                tableau1D[index++] = nombre;
            }
        }

        return tableau1D;
    }

    
    public static float[][] copierTableau(float[][] tableauOriginal) {
        
        float[][] tableauClone = new float[tableauOriginal.length][];
        for (int i = 0; i < tableauOriginal.length; i++) {
            tableauClone[i] = new float[tableauOriginal[i].length];
            System.arraycopy(tableauOriginal[i], 0, tableauClone[i], 0, tableauOriginal[i].length);
        }

        return tableauClone;
    }
}
