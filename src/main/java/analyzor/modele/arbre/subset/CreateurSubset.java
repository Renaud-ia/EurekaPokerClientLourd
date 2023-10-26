package analyzor.modele.arbre.subset;

import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.MatriceEquite;
import analyzor.modele.utils.Combinations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * classe qui va disparaître en production
 * génération de nos subsets persos et de la HashMap
 * le main crée les HashMap avec le nombre de subsets qui nous intéressent
 */
public class CreateurSubset {
    private static final List<Board> flopsGTO = getGtoFlops();

    private static List<Board> getGtoFlops() {
        List<Board> flopsGTO = new ArrayList<>();
        String cheminDuFichier = "src/main/java/analyzor/modele/arbre/subset/gtoFlopsStrings.txt";

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
            Board boardRecree = new Board(boardRandom.asInt());
            if (boardRecree.asInt() != boardRandom.asInt()) throw new RuntimeException("Problème : le board n'est pas le même");
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

    private static HashMap<Integer, Integer> tableCorrespondance(int nombreSubsets) {
        //TODO OPTIMISATION TESTER CONTRE UNE SERIE DE RANGESvsRANGES
        HashMap<Integer, Integer> tableCorrespondance = new HashMap<>();
        List<Board> subsets = recupererSubsets(nombreSubsets);

        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
        CalculatriceEquite calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        float topRange = 0.30f;
        RangeReelle rangeHero = new RangeReelle();
        rangeHero.remplir();
        GenerateurRange generateurRange = new GenerateurRange();
        RangeReelle heroRange = generateurRange.topRange(topRange);

        float bottomRange = 0.37f;
        List<RangeReelle> rangesVillains = new ArrayList<>();
        RangeReelle rangeAdverse = generateurRange.bottomRange(bottomRange);
        rangesVillains.add(rangeAdverse);

        // on précalcule les matrices des subsets
        HashMap<Integer, MatriceEquite> matricesGTO = new HashMap<>();
        for (Board subset : subsets) {
            System.out.println("Subset testé : " + subset);
            MatriceEquite equiteSubset = calculatriceEquite.equiteRange(heroRange, subset, rangesVillains);
            matricesGTO.put(subset.asInt(), equiteSubset);
        }

        // pour chaque flop GTO, on attribue le subset le plus proche
        for (Board boardGTO : flopsGTO) {
            Board subsetPlusProche = null;
            float minDistance = 1000;
            //System.out.println("Board GTO testé : " + boardGTO);

            MatriceEquite equiteBoardGTO = calculatriceEquite.equiteRange(heroRange, boardGTO, rangesVillains);
            //System.out.println(equiteBoardGTO);
            for (Board subset : subsets) {
                MatriceEquite equiteSubset = matricesGTO.get(subset.asInt());

                //System.out.println(equiteSubset);
                float distance = equiteSubset.distance(equiteBoardGTO);
                //System.out.println("Distance : " + distance);
                if (distance < minDistance) {
                    minDistance = distance;
                    subsetPlusProche = subset;
                }
            }

            System.out.println("\n*******RESULTAT********");
            System.out.println("Board GTO testé : " + boardGTO);
            System.out.println("Meilleure correspondance : " + subsetPlusProche);
            assert subsetPlusProche != null;
            tableCorrespondance.put(boardGTO.gtoRank(), subsetPlusProche.asInt());
        }

        return tableCorrespondance;
    }

    /**
     * récupère simplement la liste des subsets stockés
     * TODO : calculer des subsets
     */
    private static List<Board> recupererSubsets(int nombreSubsets) {
        StringBuilder cheminDuFichier = new StringBuilder("src/main/java/analyzor/modele/arbre/subset/");
        cheminDuFichier.append("subsets");
        cheminDuFichier.append(nombreSubsets);
        cheminDuFichier.append(".txt");

        List<Board> subsets = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(cheminDuFichier.toString()))) {
            String ligne;

            while ((ligne = br.readLine()) != null) {
                Board boardSubset = new Board(ligne);
                subsets.add(boardSubset);
            }
        }
        catch (IOException e) {e.printStackTrace();
        }

        return subsets;
    }


    public static void main(String[] args) {
        HashMap<Integer, Integer> tableSubsets15 = tableCorrespondance(15);
        GestionnaireSubset.enregistrerTableSubsets(tableSubsets15);

        HashMap<Integer, Integer> tableSubsets44 = tableCorrespondance(44);
        GestionnaireSubset.enregistrerTableSubsets(tableSubsets44);

        HashMap<Integer, Integer> tableSubsets89 = tableCorrespondance(111);
        GestionnaireSubset.enregistrerTableSubsets(tableSubsets89);
    }
}
