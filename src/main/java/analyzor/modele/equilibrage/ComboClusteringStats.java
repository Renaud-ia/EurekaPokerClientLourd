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

                    thisCell.setCellValue(listeCombos.get(j).strCompacte());
                } else if (j == -1 && i > -1) {

                    thisCell.setCellValue(listeCombos.get(i).strCompacte());
                } else if (i > -1 && j > -1) {

                    EquiteFuture equiteI = tableEquite.get(listeCombos.get(i));
                    EquiteFuture equiteJ = tableEquite.get(listeCombos.get(j));
                    float distance = equiteJ.distance(equiteI);
                    thisCell.setCellValue(distance);
                }

            }
        }

        enregistrerFichier();
    }

    private void enregistrerFichier() {
        String filePath = "equitesComboIso.xls";

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {

            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {

                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void remplirTableEquite() {
        tableEquite = new HashMap<>();
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(2, TourMain.Round.PREFLOP);
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
