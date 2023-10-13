package analyzor.modele.utils;

public class Bits {
    public static int bitsNecessaires(int nombreValeurs) {
        return ((int)(Math.log(nombreValeurs) / Math.log(2))) + 1;
    }

    public static int creerMasque(int zeroBytes, int oneBytes) {
        int mask = 0;

        // Ajouter les bits à 0
        for (int i = 0; i < zeroBytes; i++) {
            mask <<= 1;
        }

        // Ajouter les bits à 1
        for (int i = 0; i < oneBytes; i++) {
            mask <<= 1;
            mask |= 1;
        }

        return mask;
    }
}
