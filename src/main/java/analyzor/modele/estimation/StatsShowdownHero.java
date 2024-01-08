package analyzor.modele.estimation;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.classificateurs.ClassificateurFactory;
import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.*;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.bdd.ConnexionBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static analyzor.modele.estimation.Estimateur.obtenirLesSituationsTriees;

public class StatsShowdownHero {
    private final Workbook workbook;
    private ArbreAbstrait arbreAbstrait;
    private int indexSheet;
    private final CalculatriceEquite calculatriceEquite;
    public StatsShowdownHero() {
        workbook = new HSSFWorkbook();
        indexSheet = 0;
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeRapide();
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
    }
    public void construireFichierExcel(TourMain.Round round) {
        Session session = ConnexionBDD.ouvrirSession();

        FormatSolution formatSolution = recupererFormat(session);
        ProfilJoueur profilHero = recupererProfilHero(session);

        ConnexionBDD.fermerSession(session);

        arbreAbstrait = new ArbreAbstrait(formatSolution);

        LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> situationsTriees =
                obtenirLesSituationsTriees(formatSolution, round);
        int compte = 0;
        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            List<NoeudDenombrable> situationsIso = null;
            List<Entree> entreesNoeudAbstrait = GestionnaireFormat.getEntrees(formatSolution,
                    situationsTriees.get(noeudAbstrait), profilHero);
            try {
                Classificateur classificateur =
                        ClassificateurFactory.creeClassificateur(round, noeudAbstrait.getRang(), formatSolution, profilHero);
                if (classificateur == null) continue;
                classificateur.creerSituations(entreesNoeudAbstrait);
                situationsIso = classificateur.obtenirSituations();
            }
            catch (NonImplemente e) { continue; }


            if (situationsIso.isEmpty()) continue;

            for (NoeudDenombrable noeudDenombrable : situationsIso) {
                noeudDenombrable.constructionTerminee();
                if (round == TourMain.Round.PREFLOP) genererStatsPreflop(noeudDenombrable);
                else genererStatsPostflop(noeudDenombrable);
            }
        }
        enregistrerFichier(round);
    }

    private void enregistrerFichier(TourMain.Round round) {
        String filePath = "statsShowdown" + round.toString() + ".xls";

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

    private void genererStatsPostflop(NoeudDenombrable noeudDenombrable) {
    }

    private void genererStatsPreflop(NoeudDenombrable noeudDenombrable) {
        String nomSheet = indexSheet++ + "." + noeudDenombrable.toString();
        /*
        System.out.println("CREATION SHEETS POUR NOEUD : " + nomSheet);
        Sheet sheetGlobal = workbook.createSheet(nomSheet + "_GLOBAL");
        int indexAction = 0;
        for (NoeudAction noeudAction : noeudDenombrable.getNoeudsActions()) {
            Row row = sheetGlobal.createRow(indexAction);
            Cell nomAction = row.createCell(0);
            nomAction.setCellValue(noeudAction.toString());

            Cell observations = row.createCell(1);
            observations.setCellValue(noeudDenombrable.getObservation(indexAction));

            Cell showdown = row.createCell(2);
            showdown.setCellValue(noeudDenombrable.getShowdown(indexAction));

            indexAction++;
        }
         */

        Sheet sheetDetail = workbook.createSheet(nomSheet + "_DETAIL");
        Row combo = sheetDetail.createRow(0);
        combo.createCell(0).setCellValue("COMBO");
        Row equite = sheetDetail.createRow(1);
        equite.createCell(0).setCellValue("EQUITE");
        Row showdown = sheetDetail.createRow(2);
        showdown.createCell(0).setCellValue("% SHOWDOWN (sans fold)");
        Row totalCombos = sheetDetail.createRow(3);
        totalCombos.createCell(0).setCellValue("TOTAL SERVIS");
        Row combosTheoriques = sheetDetail.createRow(4);
        combosTheoriques.createCell(0).setCellValue("SERVIS THEORIQUES");

        int indexCol = 5;
        Row[] showdowns = new Row[noeudDenombrable.getNombreActions()];
        Row[] pctAction = new Row[noeudDenombrable.getNombreActions()];

        int indexAction = 0;
        for (NoeudAction noeudAction : noeudDenombrable.getNoeudsActions()) {
            pctAction[indexAction] = sheetDetail.createRow(indexCol++);
            pctAction[indexAction].createCell(0).setCellValue(noeudAction.toString() + "_compte");
            showdowns[indexAction] = sheetDetail.createRow(indexCol++);
            showdowns[indexAction].createCell(0).setCellValue(noeudAction.toString() + "_%showdown");

            if (noeudDenombrable.getEntrees(noeudAction).size() != noeudDenombrable.getObservation(noeudAction))
                throw new RuntimeException("Le nombre d'entrées ne correspond pas aux observations");

            indexAction++;
            indexCol++;
        }

        Board board = new Board();
        GenerateurRange generateurRange = new GenerateurRange();
        RangeReelle rangeVillain = generateurRange.topRange(0.37f);
        List<RangeReelle> rangesVillains = new ArrayList<>();
        rangesVillains.add(rangeVillain);

        int totalCombosTrouves = 0;
        int indexCombo = 1;
        System.out.println("DECOMPTE DES COMBOS");
        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            int compteTotal = 0;
            float showdownTotal = 0f;
            int compteJouesTotal = 0;

            combo.createCell(indexCombo).setCellValue(comboIso.codeReduit());
            float equiteFloat = calculatriceEquite.equiteGlobaleMain(comboIso.toCombosReels().get(0), board, rangesVillains);
            equite.createCell(indexCombo).setCellValue(equiteFloat);

            for (int i = 0; i < noeudDenombrable.getNoeudsActions().length; i++) {
                NoeudAction noeudAction = noeudDenombrable.getNoeudsActions()[i];
                int compteObservation = 0;
                int compteShowdown = 0;

                for (Entree entree : noeudDenombrable.getEntrees(noeudAction)) {
                    MainEnregistree mainEnregistree = entree.getTourMain().getMain();
                    String hero = mainEnregistree.getPartie().getNomHero();
                    if (!Objects.equals(entree.getJoueur().getNom(), hero)){
                        throw new RuntimeException("c'est pas hero");
                    }
                    for (ComboReel comboReel : comboIso.toCombosReels()) {
                        if (entree.getCombo() == comboReel.toInt()) {
                            compteShowdown++;
                        }
                        if (mainEnregistree.getComboHero() == 0) System.out.println("ERREUR : COMBO VAUT 0");
                        if (mainEnregistree.getComboHero() == comboReel.toInt()) {
                            compteObservation++;
                            totalCombosTrouves++;
                            if (noeudAction.getMove() != Move.FOLD) {
                                compteJouesTotal++;
                            }
                        }
                    }
                }

                showdowns[i].createCell(indexCombo).setCellValue((double) compteShowdown / compteObservation);
                pctAction[i].createCell(indexCombo).setCellValue(compteObservation);
                compteTotal += compteObservation;
                showdownTotal += compteShowdown;
            }
            showdown.createCell(indexCombo).setCellValue(showdownTotal / compteJouesTotal);
            totalCombos.createCell(indexCombo).setCellValue(compteTotal);
            combosTheoriques.createCell(indexCombo).setCellValue((double) comboIso.toCombosReels().size() * noeudDenombrable.totalEntrees() / 1326);

            indexCombo++;
        }

        System.out.println("TOTAL COMBOS TROUVES : " + totalCombosTrouves);
        System.out.println("TOTAL ENTREES : " + noeudDenombrable.totalEntrees());

        sheetDetail.createRow(indexCol + 2).createCell(0).setCellValue("TOTAL SERVIS");
        sheetDetail.createRow(indexCol + 2).createCell(1).setCellValue(totalCombosTrouves);

    }

    private FormatSolution recupererFormat(Session session) {
        Variante.PokerFormat pokerFormat = Variante.PokerFormat.SPIN;
        return new FormatSolution(pokerFormat, false, false, 3, 0, 100);
    }

    private ProfilJoueur recupererProfilHero(Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ProfilJoueur> criteria = builder.createQuery(ProfilJoueur.class);
        Root<ProfilJoueur> root = criteria.from(ProfilJoueur.class);
        criteria.select(root).where(builder.equal(root.get("hero"), true));

        return session.createQuery(criteria).uniqueResult();
    }

    public static void main(String[] args) {
        TourMain.Round round = TourMain.Round.PREFLOP;
        StatsShowdownHero statsShowdownHero = new StatsShowdownHero();
        statsShowdownHero.construireFichierExcel(round);
    }
}
