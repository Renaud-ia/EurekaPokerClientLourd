package analyzor.modele.utils;

public class Bits {
    public static int bitsNecessaires(int nombreValeurs) {
        return ((int)(Math.log(nombreValeurs) / Math.log(2))) + 1;
    }

    public static int compterBits(long value) {
        // Si la valeur est 0, elle occupe un seul bit
        if (value == 0) {
            return 1;
        }

        int count = 0;
        while (value != 0) {
            count++;
            value >>>= 1; // Décalage d'un bit vers la droite
        }

        return count;
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
