package analyzor.controleur;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.licence.LicenceManager;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.poker.RangeReelle;
import analyzor.modele.simulation.*;
import analyzor.vue.donnees.*;
import analyzor.vue.donnees.table.*;
import analyzor.vue.reutilisables.fenetres.FenetreChargement;
import analyzor.vue.table.FenetreConfiguration;
import analyzor.vue.table.VueTable;
import analyzor.vue.FenetrePrincipale;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;


public class ControleurTable implements ControleurSecondaire {
    private final FenetrePrincipale fenetrePrincipale;
    private final ControleurPrincipal controleurPrincipal;
    private final TableSimulation tableSimulation;
    private final VueTable vueTable;
    private final FenetreConfiguration fenetreConfiguration;
    private final InfosSolution infosSolution;
    private final ConfigTable configTable;
    
    private final LinkedList<DTOSituation> situations;
    private final HashMap<TablePoker.JoueurTable, DTOJoueur> mappageJoueurs;
    private final RangeVisible rangeVisible;
    private FormatSolution formatSolutionActuel;
    private final GestionnaireCalculEquite threadCalculEquite;

    ControleurTable(FenetrePrincipale fenetrePrincipale, ControleurPrincipal controleurPrincipal) {
        
        configTable = new ConfigTable();
        infosSolution = new InfosSolution();
        situations = new LinkedList<>();
        mappageJoueurs = new HashMap<>();
        rangeVisible = new RangeVisible();

        this.fenetrePrincipale = fenetrePrincipale;
        this.controleurPrincipal = controleurPrincipal;
        vueTable = new VueTable(fenetrePrincipale, this, infosSolution, situations, rangeVisible);

        fenetreConfiguration = new FenetreConfiguration(fenetrePrincipale, this, configTable);
        tableSimulation = new TableSimulation();
        threadCalculEquite = new GestionnaireCalculEquite();
    }

    

    public void clickModeHU(boolean modeHU) {
        tableSimulation.modeHU(formatSolutionActuel, modeHU);
        this.initialiserJoueurs();
        fenetreConfiguration.afficher();
    }

    public void clickSolution() {
        controleurPrincipal.gererFormats();
    }

    public void clickGestionTable() {
        fenetreConfiguration.afficher();
    }

    
    public void clickAction(DTOSituationTrouvee dtoSituationTrouvee, int indexAction) {
        final FenetreChargement fenetreChargement =
                new FenetreChargement(fenetrePrincipale, "Chargement des donn\u00E9es...");

        Thread actualisationtable = new Thread(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(fenetreChargement::lancer);
                int indexVueSituation = situations.indexOf(dtoSituationTrouvee);
                if (indexVueSituation == -1) throw new IllegalArgumentException("Situation non trouvée");

                tableSimulation.changerAction(dtoSituationTrouvee.getSituationModele(), indexAction);
                deselectionnerActionsSuivantes(dtoSituationTrouvee, indexAction);

                reconstruireSituations(indexVueSituation);
                selectionnerSituation(indexVueSituation + 1);

                selectionnerActionDansVue(dtoSituationTrouvee, indexAction);
                SwingUtilities.invokeLater(fenetreChargement::arreter);
            }
        });

        actualisationtable.start();
    }

    public void clickSituation(DTOSituation dtoSituationTrouvee) {
        final FenetreChargement fenetreChargement =
                new FenetreChargement(fenetrePrincipale, "Chargement des donn\u00E9es...");

        Thread actualisationtable = new Thread(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(fenetreChargement::lancer);
                int indexVueSituation = situations.indexOf(dtoSituationTrouvee);
                if (indexVueSituation == -1) throw new IllegalArgumentException("Situation non trouvée");
                selectionnerSituation(indexVueSituation);
                SwingUtilities.invokeLater(fenetreChargement::arreter);
            }
        });

        actualisationtable.start();
    }

    @Deprecated
    public void clickBoard() {

    }

    
    public void clickActionsStats(int indexAction) {
        if (rangeVisible.getActionSelectionnee() == null || indexAction != rangeVisible.getActionSelectionnee()) {
            rangeVisible.setActionSelectionnee(indexAction);
        }
        else {
            rangeVisible.setActionSelectionnee(null);
        }
        vueTable.actualiserVueRange();
    }

    public void clickCombo(String nomCombo) {
        actualiserVueCombo(nomCombo);
    }

    
    public void formatSelectionne(FormatSolution formatSolution) {
        final FenetreChargement fenetreChargement =
                new FenetreChargement(fenetrePrincipale, "Chargement des donn\u00E9es...");
        Thread actualisationtable = new Thread(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(fenetreChargement::lancer);
                formatSolutionActuel = formatSolution;
                configTable.setBounty(formatSolution.getKO());
                configTable.setPokerFormat(formatSolution.getPokerFormat());
                tableSimulation.setFormatSolution(formatSolution);
                infosSolution.setVariante(formatSolution.getPokerFormat().codeReduit());
                infosSolution.setnJoueurs(formatSolution.getNombreJoueurs());
                infosSolution.setBounty(formatSolution.getKO());
                
                vueTable.actualiserSolution();
                initialiserJoueurs();
                initialiserSituations();
                SwingUtilities.invokeLater(fenetreChargement::arreter);
            }
        });

        actualisationtable.start();
    }

    
    public void configurationSelectionnee() {
        final FenetreChargement fenetreChargement = new FenetreChargement(fenetrePrincipale, "Chargement des donn\u00E9es...");
        Thread actualisationtable = new Thread(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(fenetreChargement::lancer);
                
                
                for (DTOJoueur joueurDepart : configTable.getJoueurs()) {
                    TablePoker.JoueurTable joueurModele = joueurDepart.getJoueurModele();
                    tableSimulation.resetJoueur(joueurModele);
                    tableSimulation.setStack(joueurModele, joueurDepart.getStack());
                    tableSimulation.setHero(joueurModele, joueurDepart.getHero());
                    tableSimulation.setBounty(joueurModele, joueurDepart.getBounty());
                }

                if (tableSimulation.reconstruireSituations()) {
                    
                    
                    initialiserJoueurs();
                    initialiserSituations();
                }
                SwingUtilities.invokeLater(fenetreChargement::arreter);
            }
        });

        actualisationtable.start();
    }

    

    private void initialiserJoueurs() {
        configTable.viderJoueurs();
        mappageJoueurs.clear();

        float stackMoyen = 0;
        for (TablePoker.JoueurTable joueurSimulation : tableSimulation.getJoueurs()) {
            DTOJoueur dtoJoueur = new DTOJoueur(joueurSimulation, joueurSimulation.getNom(),
                    joueurSimulation.estHero(), joueurSimulation.getBounty(), joueurSimulation.getStackInitial());
            mappageJoueurs.put(joueurSimulation, dtoJoueur);
            configTable.ajouterJoueur(dtoJoueur);

            stackMoyen += dtoJoueur.getStack() / tableSimulation.getJoueurs().size();
        }
        vueTable.actualiserConfigTable(stackMoyen);
    }

    private void initialiserSituations() {
        reconstruireSituations(0);
        selectionnerSituation(0);
    }

    
    private void etatParDefautSituationPrecedente(int indexVueSituation) {
        for (int i = 0; i < indexVueSituation; i++) {
            DTOSituationTrouvee dtoSituationTrouvee;
            DTOSituation dtoSituation = situations.get(i);
            if (dtoSituation == null) throw new RuntimeException("Aucun DTO trouvé");

            
            if (!(dtoSituation instanceof DTOSituationTrouvee)) return;
            else dtoSituationTrouvee = (DTOSituationTrouvee) dtoSituation;

            Integer indexAction = tableSimulation.fixerActionParDefaut(dtoSituationTrouvee.getSituationModele());
            
            if (indexAction != null) {
                selectionnerActionDansVue(dtoSituationTrouvee, indexAction);
            }
        }
    }

    
    private void reconstruireSituations(int indexVueSituation) {
        SimuSituation situationModele;

        if (indexVueSituation == 0) {
            for (DTOSituation situationSupprimee : situations) {
                vueTable.supprimerSituation(situationSupprimee);
            }
            situations.clear();
            situationModele = null;
        }

        else {
            DTOSituation dtoSituation = situations.get(indexVueSituation);
            if (!(dtoSituation instanceof DTOSituationTrouvee)) {
                situationModele = null;
            } else {
                situationModele = ((DTOSituationTrouvee) dtoSituation).getSituationModele();
            }

            
            int dernierIndex = situations.size();
            List<DTOSituation> situationsSupprimees = situations.subList(indexVueSituation, dernierIndex);
            for (DTOSituation situationSupprimee : situationsSupprimees) {
                vueTable.supprimerSituation(situationSupprimee);
            }
            situationsSupprimees.clear();
        }


        
        LinkedList<SimuSituation> situationsChangees = tableSimulation.situationsSuivantes(situationModele);
        for (SimuSituation nouvelleSituation : situationsChangees) {
            
            if (LicenceManager.getInstance().modeDemo() && !(situations.isEmpty())) {
                break;
            }
            TablePoker.JoueurTable joueurSimulation = nouvelleSituation.getJoueur();
            DTOJoueur dtoJoueur = mappageJoueurs.get(joueurSimulation);
            
            if (dtoJoueur == null) {
                throw new RuntimeException("Joueur non trouvé");
            }
            DTOSituationTrouvee nouvelleCase =
                    new DTOSituationTrouvee(nouvelleSituation, dtoJoueur,
                            nouvelleSituation.getStack());
            
            for (SimuAction simuAction : nouvelleSituation.getActions()) {
                nouvelleCase.ajouterAction(simuAction.getMove(), simuAction.getBetSize(), simuAction.getIndex());
            }
            ajouterSituation(nouvelleCase);
        }

        if (LicenceManager.getInstance().modeDemo()) {
            DTODemo dtoInfo = new DTODemo(
                    "Aucune licence");
            ajouterSituation(dtoInfo);
        }
        else if (tableSimulation.leafTrouvee()) {
            DTOLeaf dtoLeaf = new DTOLeaf();
            ajouterSituation(dtoLeaf);
        }
        else {
            DTOSituationNonTrouvee dtoInfo = new DTOSituationNonTrouvee(
                    "Range non trouv\u00E9e");
            ajouterSituation(dtoInfo);
        }

    }

    

    private void ajouterSituation(DTOSituation nouvelleCase) {
        situations.add(nouvelleCase);
        vueTable.ajouterSituation(nouvelleCase);
    }

    private void selectionnerSituation(int indexVueSituation) {
        DTOSituation situation;
        try {
            situation = situations.get(indexVueSituation);
        }
        
        catch (IndexOutOfBoundsException e) {
            
            indexVueSituation--;
            situation = situations.get(indexVueSituation);
        }

        
        etatParDefautSituationPrecedente(indexVueSituation);
        
        vueTable.selectionnerSituation(situation);

        if (situation instanceof DTOSituationTrouvee) {
            
            tableSimulation.setSituationSelectionnee(((DTOSituationTrouvee) situation).getSituationModele());
            
            List<RangeReelle> rangesVillains = tableSimulation.getRangesVillains();
            threadCalculEquite.setRangesVillains(rangesVillains);
            actualiserRange();
        }

        else {
            if (indexVueSituation != 0) selectionnerSituation(indexVueSituation - 1);

            
            if (situation instanceof DTOSituationErreur) {
                if (situation instanceof DTODemo) {
                    fenetrePrincipale.messageInfo("Aucune licence n'est activ\u00E9e. \n" +
                            "Pour consulter les ranges suivantes veuillez activer une licence");
                }
                else if (situation instanceof DTOSituationNonTrouvee) {
                    fenetrePrincipale.messageInfo("La range n'a pas \u00E9t\u00E9 trouv\u00E9e. \n" +
                            "Vous n'avez peut-\u00EAtre pas calcul\u00E9 tout le format, \n" +
                            "ou vous n'avez pas assez de donn\u00E9es pour ce format. \n" +
                            "Consultez notre site pour plus d'informations");
                }
            }
        }


    }

    
    private void actualiserRange() {
        rangeVisible.reset();

        
        LinkedHashMap<SimuAction, RangeIso> ranges = tableSimulation.getRangesSituationActuelle(null);

        for (SimuAction simuAction : ranges.keySet()) {
            int rangAction = rangeVisible.ajouterAction(simuAction.getMove(), simuAction.getBetSize());
            RangeIso rangeAction = ranges.get(simuAction);
            if (rangeAction == null) {
                vueTable.viderRange();
                return;
            }
            for (ComboIso comboIso : rangeAction.getCombos()) {
                rangeVisible.ajouterValeurCombo(
                        rangAction, comboIso.codeReduit(), comboIso.getValeur(), comboIso.getNombreCombos());
            }
        }

        rangeVisible.setActionSelectionnee(null);
        actualiserVueCombo(null);


        vueTable.actualiserVueRange();
    }

    private void actualiserVueCombo(String nomCombo) {
        if (!rangeVisible.estVide()) {

            if (nomCombo == null) {
                nomCombo = rangeVisible.selectionnerComboDefaut();
            } else {
                rangeVisible.setComboSelectionne(nomCombo);
            }
            threadCalculEquite.lancerCalcul(vueTable.getCaseComboStats(), new ComboIso(nomCombo));

        }

        vueTable.actualiserVueCombo();
    }

    
    private void selectionnerActionDansVue(DTOSituationTrouvee dtoSituationTrouvee, int indexAction) {
        dtoSituationTrouvee.setActionSelectionnee(indexAction);
        vueTable.selectionnerAction(dtoSituationTrouvee, indexAction);
    }

    private void deselectionnerActionsSuivantes(DTOSituationTrouvee dtoSituationTrouvee, int indexAction) {
        dtoSituationTrouvee.deselectionnerAction();
        vueTable.deselectionnerAction(dtoSituationTrouvee, indexAction);
    }

    @Override
    public void demarrer() {
        
    }

    @Override
    public void lancerVue() {
        vueTable.setVisible(true);
    }

    @Override
    public void desactiverVue() {
        vueTable.setVisible(false);
    }

    public void redimensionnerRange() {
        vueTable.redimensionnerRange();
    }
}
