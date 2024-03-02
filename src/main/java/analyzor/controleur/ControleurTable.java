package analyzor.controleur;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.licence.LicenceManager;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.simulation.*;
import analyzor.vue.donnees.*;
import analyzor.vue.donnees.table.*;
import analyzor.vue.table.FenetreConfiguration;
import analyzor.vue.table.VueTable;
import analyzor.vue.FenetrePrincipale;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Controleur qui fait l'interface entre PokerTable et la VueTable
 * PokerTable et VueTable gardent chacun en mémoire l'état actuel des actions
 * ControleurTable garantit que les deux sont identiques
 * todo : il faudrait refaire l'architecture, pour l'instant le controleur gère manuellement l'équivalence entre objets du modèle et de la vue
 *
 */
public class ControleurTable implements ControleurSecondaire {
    private final ControleurPrincipal controleurPrincipal;
    private final TableSimulation tableSimulation;
    private final VueTable vueTable;
    private final FenetreConfiguration fenetreConfiguration;
    private final InfosSolution infosSolution;
    private final ConfigTable configTable;
    // stocke l'ordre d'affichage des actions
    private final LinkedList<DTOSituation> situations;
    private final HashMap<TablePoker.JoueurTable, DTOJoueur> mappageJoueurs;
    private final RangeVisible rangeVisible;
    private FormatSolution formatSolutionActuel;

    ControleurTable(FenetrePrincipale fenetrePrincipale, ControleurPrincipal controleurPrincipal) {
        // DTO
        configTable = new ConfigTable();
        infosSolution = new InfosSolution();
        situations = new LinkedList<>();
        mappageJoueurs = new HashMap<>();
        rangeVisible = new RangeVisible();

        this.controleurPrincipal = controleurPrincipal;
        vueTable = new VueTable(fenetrePrincipale, this, infosSolution, situations, rangeVisible);

        fenetreConfiguration = new FenetreConfiguration(fenetrePrincipale, this, configTable);
        tableSimulation = new TableSimulation();
    }

    // méthodes publiques correspondant aux différents interactions de l'user

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

    // interface utilisée par la vue pour signifier qu'on a clické sur une action dans le bandeau de situations
    public void clickAction(DTOSituationTrouvee dtoSituationTrouvee, int indexAction) {
        System.out.println("CLICK ACTION : " + dtoSituationTrouvee.getActions());
        System.out.println("Nombre de situations stockées : " + situations.size());
        int indexVueSituation = situations.indexOf(dtoSituationTrouvee);
        if (indexVueSituation == -1) throw new IllegalArgumentException("Situation non trouvée");

        tableSimulation.changerAction(dtoSituationTrouvee.getSituationModele(), indexAction);
        deselectionnerActionsSuivantes(dtoSituationTrouvee, indexAction);

        reconstruireSituations(indexVueSituation);
        selectionnerSituation(indexVueSituation + 1);

        selectionnerActionDansVue(dtoSituationTrouvee, indexAction);
    }

    public void clickSituation(DTOSituation dtoSituationTrouvee) {
        System.out.println("CLICK SITUATION");
        int indexVueSituation = situations.indexOf(dtoSituationTrouvee);
        if (indexVueSituation == -1) throw new IllegalArgumentException("Situation non trouvée");
        selectionnerSituation(indexVueSituation);
    }

    @Deprecated
    public void clickBoard() {

    }

    // on a clické sur une action sur les stats, on ne va afficher que cette action là
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

    /**
     * méthode lancée quand on sélectionne un format sélection
     * on se fait pas chier, on reconstruit toute la série d'actions
     * car va être galère si pas d'équivalence
     * todo : on pourrait faire la même série d'action si elle existe
     */
    public void formatSelectionne(FormatSolution formatSolution) {
        formatSolutionActuel = formatSolution;
        // todo que faire si on a aucun formatSolution
        configTable.setBounty(formatSolution.getKO());
        tableSimulation.setFormatSolution(formatSolution);
        infosSolution.setVariante(formatSolution.getPokerFormat().name());
        infosSolution.setnJoueurs(formatSolution.getNombreJoueurs());
        infosSolution.setBounty(formatSolution.getKO());
        // on va rafraichir l'affichage de la solution, initialiser les joueurs et les situations
        vueTable.actualiserSolution();
        initialiserJoueurs();
        initialiserSituations();
    }

    /**
     * méthode lancée quand on sélectionne une configuration
     * on se fait pas chier, on reconstruit toute la série d'actions
     * car va être galère si pas d'équivalence
     * todo : on pourrait faire la même série d'action si elle existe
     */
    public void configurationSelectionnee() {
        // c'est la vue qui modifie le DTO, on va dire que c'est ok
        // on actualise d'abord la table
        for (DTOJoueur joueurDepart : configTable.getJoueurs()) {
            TablePoker.JoueurTable joueurModele = joueurDepart.getJoueurModele();
            tableSimulation.resetJoueur(joueurModele);
            tableSimulation.setStack(joueurModele, joueurDepart.getStack());
            tableSimulation.setHero(joueurModele, joueurDepart.getHero());
            tableSimulation.setBounty(joueurModele, joueurDepart.getBounty());
        }
        tableSimulation.reconstruireSituations();
        // puis la vue
        // l'icone ne change pas dans la vue
        initialiserJoueurs();
        initialiserSituations();
    }

    // méthode privées de logique interne

    private void initialiserJoueurs() {
        configTable.viderJoueurs();
        mappageJoueurs.clear();
        for (TablePoker.JoueurTable joueurSimulation : tableSimulation.getJoueurs()) {
            DTOJoueur dtoJoueur = new DTOJoueur(joueurSimulation, joueurSimulation.getNom(),
                    joueurSimulation.estHero(), joueurSimulation.getBounty(), joueurSimulation.getStackInitial());
            mappageJoueurs.put(joueurSimulation, dtoJoueur);
            configTable.ajouterJoueur(dtoJoueur);
        }
        vueTable.actualiserConfigTable();
    }

    private void initialiserSituations() {
        reconstruireSituations(0);
        selectionnerSituation(0);
    }

    /**
     * on vérifie que toutes les situations précédentes ont une action sélectionnée
     * sinon par défaut
     * on doit actualiser TablePoker ET VueTable
     */
    private void etatParDefautSituationPrecedente(int indexVueSituation) {
        for (int i = 0; i < indexVueSituation; i++) {
            DTOSituationTrouvee dtoSituationTrouvee;
            DTOSituation dtoSituation = situations.get(i);
            if (dtoSituation == null) throw new RuntimeException("Aucun DTO trouvé");

            // si n'est pas une situation non trouvé il n'y a rien à faire
            if (!(dtoSituation instanceof DTOSituationTrouvee)) return;
            else dtoSituationTrouvee = (DTOSituationTrouvee) dtoSituation;

            Integer indexAction = tableSimulation.fixerActionParDefaut(dtoSituationTrouvee.getSituationModele());
            // si c'est pas nul, ça veut dire que l'action n'était pas fixée
            if (indexAction != null) {
                selectionnerActionDansVue(dtoSituationTrouvee, indexAction);
            }
        }
    }

    /**
     * reconstruit les situations à partir de l'index situation
     */
    private void reconstruireSituations(int indexVueSituation) {
        System.out.println("RECONSTRUCTION SITUATIONS INDEX : " + indexVueSituation);
        System.out.println("CONTENU SITUATIONS : " + situations);
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

            // on supprime toutes les situations qui suivent
            int dernierIndex = situations.size();
            List<DTOSituation> situationsSupprimees = situations.subList(indexVueSituation, dernierIndex);
            for (DTOSituation situationSupprimee : situationsSupprimees) {
                vueTable.supprimerSituation(situationSupprimee);
            }
            situationsSupprimees.clear();
        }


        // on ajoute les nouvelle situations
        LinkedList<SimuSituation> situationsChangees = tableSimulation.situationsSuivantes(situationModele);
        for (SimuSituation nouvelleSituation : situationsChangees) {
            // on limite la vue à une situation si mode démo
            if (LicenceManager.getInstance().modeDemo() && !(situations.isEmpty())) {
                break;
            }
            TablePoker.JoueurTable joueurSimulation = nouvelleSituation.getJoueur();
            DTOJoueur dtoJoueur = mappageJoueurs.get(joueurSimulation);
            // si le mappage du joueur n'existe pas, il y a une erreur quelque part
            if (dtoJoueur == null) {
                throw new RuntimeException("Joueur non trouvé");
            }
            DTOSituationTrouvee nouvelleCase =
                    new DTOSituationTrouvee(nouvelleSituation, dtoJoueur,
                            nouvelleSituation.getStack());
            // on ajoute les actions possibles dans la situation
            for (SimuAction simuAction : nouvelleSituation.getActions()) {
                nouvelleCase.ajouterAction(simuAction.getMove(), simuAction.getBetSize(), simuAction.getIndex());
            }
            ajouterSituation(nouvelleCase);
            System.out.println("SITUATION AJOUTEE PAR CONTROLEUR : " + nouvelleCase);
            System.out.println("SIMU SITUATION : " + nouvelleSituation);
        }

        // todo ajouter les cases démo/rangeNonTrouvée/leaf selon les cas
        if (LicenceManager.getInstance().modeDemo()) {
            DTOInfo dtoInfo = new DTOInfo("VERSION DEMO");
            ajouterSituation(dtoInfo);
        }
        else if (tableSimulation.leafTrouvee()) {
            DTOLeaf dtoLeaf = new DTOLeaf();
            ajouterSituation(dtoLeaf);
        }
        else {
            DTOInfo dtoInfo = new DTOInfo("RANGE \n NON \n TROUVEE");
            ajouterSituation(dtoInfo);
        }

    }

    // méthodes de contrôle de la vue

    private void ajouterSituation(DTOSituation nouvelleCase) {
        situations.add(nouvelleCase);
        vueTable.ajouterSituation(nouvelleCase);
    }

    private void selectionnerSituation(int indexVueSituation) {
        DTOSituation situation;
        try {
            situation = situations.get(indexVueSituation);
        }
        // parfois la situation suivante n'existe pas car on est sur une leaf
        catch (IndexOutOfBoundsException e) {
            // todo ici plutôt : on veut afficher si non trouvé, mode démo ou leaf
            // un peu dégueu de créer de nouvelles situations en fait mais comme ça l'interface existe
            indexVueSituation--;
            situation = situations.get(indexVueSituation);
        }

        // on sélectionne un état par défaut si il n'y en a pas pour les situations antérieures
        etatParDefautSituationPrecedente(indexVueSituation);
        // on informe la vue que cette situation est sélectionnée
        vueTable.selectionnerSituation(situation);

        // si la situation est flop, démo ou non trouvée, on affiche le bon message dans la range
        if (!(situation instanceof DTOSituationTrouvee)) {
            rangeVisible.reset();
            rangeVisible.setMessage("SALUT");
            vueTable.actualiserVueRange();
        }

        else {
            // sinon on affiche range comme prévue
            tableSimulation.setSituationSelectionnee(((DTOSituationTrouvee ) situation).getSituationModele());
            actualiserRange(null);
        }
    }

    /**
     * méthode utilisée pour construire la range
     * @param indexAction => vaut null si pas d'action sélectionnée => dans ce cas on affiche toutes les ranges
     *  todo ne sert à rien de préciser l'index
     */
    private void actualiserRange(Integer indexAction) {
        System.out.println("ACUTALISATION RANGES");
        // la vue ne conserve pas la mémoire des ranges, seulement TablePoker donc on redemande à chaque fois
        LinkedHashMap<SimuAction, RangeIso> ranges = tableSimulation.getRanges(indexAction);
        rangeVisible.reset();
        for (SimuAction simuAction : ranges.keySet()) {
            System.out.println("RANGE ACTION : " + simuAction);
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
        if (rangeVisible.estVide()) return;
        if (nomCombo == null) {
            nomCombo = rangeVisible.selectionnerComboDefaut();
        }
        else {
            rangeVisible.setComboSelectionne(nomCombo);
        }

        if (rangeVisible.equiteInconnue(nomCombo)) {
            float equite = tableSimulation.getEquite(nomCombo);
            rangeVisible.setEquite(nomCombo, equite);
        }
        vueTable.actualiserVueCombo();
    }

    /**
     * pour éviter la gestion trop complexe, c'est le contrôleur qui dit à la vue de
     * mettre les actions comme sélectionnées
     */
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
        // todo : mettre le dernier format utilisé
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
