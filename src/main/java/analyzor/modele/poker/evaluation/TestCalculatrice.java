package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.List;

public class TestCalculatrice {
    public static void main(String[] args) {
        int nBoucles = 10000;
        List<Integer> maListe = new ArrayList<>();
        int[] monTableau = new int[nBoucles];


        for (int i = 0; i < nBoucles; i++) {
            maListe.add(i);
        }


        for (int i = 0; i < nBoucles; i++) {
            monTableau[i] = i;
        }
        for (int a : maListe) {System.out.println(a);}

        long startTime = System.nanoTime();
        for (int i = 0; i < nBoucles; i++) {System.out.println(maListe.get(i));}
        long endTime = System.nanoTime();

        double duration = endTime - startTime / 1_000_000.0;
        System.out.println(duration);
    }
}
