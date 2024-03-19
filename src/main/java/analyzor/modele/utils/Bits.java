package analyzor.modele.utils;

public class Bits {
    public static int bitsNecessaires(long nombreValeurs) {
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

    public static long creerMasque(int zeroBytes, int oneBytes) {
        long mask = 0;

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

    public static void main(String[] args) {
        System.out.println(Bits.bitsNecessaires(1023));
    }

    public static long bitsPleins(int nBits) {
        if (nBits > 64) {
            throw new IllegalArgumentException("Pas possible de coder plus de 64 bits");
        }
        return (1L << nBits) - 1;
    }
}
