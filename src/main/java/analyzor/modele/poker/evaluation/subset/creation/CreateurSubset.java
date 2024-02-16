package analyzor.modele.poker.evaluation.subset.creation;

import analyzor.modele.berkeley.EnregistrementSubset;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.MatriceEquite;
import analyzor.modele.poker.evaluation.subset.GestionnaireSubset;
import analyzor.modele.utils.Combinations;
import com.sleepycat.je.DatabaseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * todo : en production à supprimer + fichiers sources (=tout le dossier)
 * classe qui va disparaître en production
 * génération de nos subsets persos et de la HashMap
 * le main crée les HashMap avec le nombre de subsets qui nous intéressent
 */
public class CreateurSubset {
    private static final List<Board> flopsGTO = getGtoFlops();

    public static void main(String[] args) throws IOException, DatabaseException {
        EnregistrementSubset enregistrementSubset = new EnregistrementSubset();
        enregistrementSubset.enregistrerSubsets(flopsGTO);

        Integer[] subsetsVoulus = new Integer[] {15, 44, 111};

        for (int nombreSubsets : subsetsVoulus) {
            List<Board> boardsSubset = recupererSubsets(nombreSubsets);
            enregistrementSubset.enregistrerSubsets(boardsSubset);
        }

        List<Board> boardsTest = enregistrementSubset.recupererSubsets(111);
        for (Board board : boardsTest) {
            System.out.println(board);
        }
    }

    private static List<Board> getGtoFlops() {
        List<Board> flopsGTO = new ArrayList<>();
        StringBuilder chemin = cheminDossier().append("gtoFlopsStrings.txt");
        String cheminDuFichier = chemin.toString();

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

    /**
     * récupère simplement la liste des subsets stockés
     * TODO : calculer des subsets
     */
    private static List<Board> recupererSubsets(int nombreSubsets) {
        StringBuilder cheminDuFichier = cheminDossier();
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

    private static StringBuilder cheminDossier() {
        return new StringBuilder("src/main/java/analyzor/modele/poker/evaluation/subset/creation/");
    }
}
