package analyzor.modele.arbre.subset;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.GenerateurCombos;
import analyzor.modele.utils.Combinations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * classe qui va disparaître en production
 * génération de nos subsets persos et de la HashMap
 */
public class GenerateurSubset {
    private static final List<Board> flopsGTO = getGtoFlops();

    private static List<Board> getGtoFlops() {
        List<Board> flopsGTO = new ArrayList<>();
        String cheminDuFichier = "src/main/resources/gtoFlopsStrings.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(cheminDuFichier))) {
            String ligne;

            int index = 0;
            while ((ligne = br.readLine()) != null) {
                if (!ligne.equals("")) {
                    Board board = new Board(ligne);
                    flopsGTO.add(board);
                    index++;
                }
            }
            if (index != 1755) throw new IllegalArgumentException("Nombre de flops GTO incorrect");
        }
        catch (IOException e) {e.printStackTrace();
        }
        return flopsGTO;
    }

    public static void testGtoRank() {
        List<Carte> toutesLesCartes = GenerateurCombos.toutesLesCartes;
        Collections.shuffle(toutesLesCartes);

        List<Carte> echantillonCartes = toutesLesCartes.subList(0, Math.min(toutesLesCartes.size(), 20));

        Combinations<Carte> combinator = new Combinations<>(echantillonCartes);
        for (List<Carte> cartesBoard : combinator.getCombinations(3)) {
            Board boardRandom = new Board(cartesBoard);
            System.out.println("*******************");
            System.out.println(boardRandom);
            int index = 0;
            for (Board gtoBoard : flopsGTO) {
                if (gtoBoard.gtoRank() == boardRandom.gtoRank()) {
                    System.out.println("Equivalent GTO trouvé : " + gtoBoard);
                    break;
                }
                if (index == flopsGTO.size()) {
                    throw new RuntimeException("Flop GTO non trouvé");
                }
            }
        }
    }


    public static void main(String[] args) {
        testGtoRank();
    }
}
