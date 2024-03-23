package analyzor.modele.equilibrage;

import analyzor.modele.denombrement.CalculEquitePreflop;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.EquiteFuture;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * comparer les distance euclidiennes
 *
 */
public class ComboClusteringStats {
    private HashMap<ComboIso, EquiteFuture> tableEquite;
    private final Workbook workbook;
    private List<ComboIso> listeCombos;

    public ComboClusteringStats() {
        workbook = new HSSFWorkbook();
        listeCombos = GenerateurCombos.combosIso;
    }

    private void construireFeuilleExcel() {
        remplirTableEquite();
        Sheet sheetDetail = workbook.createSheet("distancesEquites");

        for (int i = -1; i < listeCombos.size(); i++) {
            Row thisRow = sheetDetail.createRow(i + 1);
            for (int j = -1; j < listeCombos.size(); j++) {
                Cell thisCell = thisRow.createCell(j + 1);

                if (i == -1 && j > -1) {
                    // Ligne du haut avec les en-têtes de colonne
                    thisCell.setCellValue(listeCombos.get(j).strCompacte());
                } else if (j == -1 && i > -1) {
                    // Première colonne avec les en-têtes de ligne
                    thisCell.setCellValue(listeCombos.get(i).strCompacte());
                } else if (i > -1 && j > -1) {
                    // Calcul de la distance et remplissage de la cellule
                    EquiteFuture equiteI = tableEquite.get(listeCombos.get(i));
                    EquiteFuture equiteJ = tableEquite.get(listeCombos.get(j));
                    float distance = equiteJ.distance(equiteI);
                    thisCell.setCellValue(distance);
                }
                // Pas besoin d'une condition pour i == -1 && j == -1, car cette cellule peut rester vide
            }
        }

        enregistrerFichier();
    }

    private void enregistrerFichier() {
        String filePath = "equitesComboIso.xls";

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            // Écrire le contenu du workbook dans le fichier
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Fermer le Workbook pour libérer les ressources
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void remplirTableEquite() {
        tableEquite = new HashMap<>();
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(3, TourMain.Round.PREFLOP);
        CalculEquitePreflop.getInstance().setNoeudAbstrait(noeudAbstrait);

        for (ComboIso comboIso : listeCombos) {
            EquiteFuture equiteFuture = CalculEquitePreflop.getInstance().getEquite(comboIso);
            tableEquite.put(comboIso, equiteFuture);
        }
    }

    public static void main(String[] args) {
        ComboClusteringStats comboClusteringStats = new ComboClusteringStats();
        comboClusteringStats.construireFeuilleExcel();
    }
}
