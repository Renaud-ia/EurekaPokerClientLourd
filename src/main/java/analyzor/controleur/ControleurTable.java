package analyzor.controleur;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.simulation.*;
import analyzor.vue.donnees.*;
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

    public void clickSolution() {
        controleurPrincipal.gererFormats();
    }

    public void clickGestionTable() {
        fenetreConfiguration.afficher();
    }

    // interface utilisée par la vue pour signifier qu'on a clické sur une action
    public void clickAction(DTOSituation dtoSituation, int indexAction) {
        System.out.println("CLICK ACTION : " + dtoSituation.getActions());
        System.out.println("Nombre de situations stockées : " + situations.size());
        int indexVueSituation = situations.indexOf(dtoSituation);
        if (indexVueSituation == -1) throw new IllegalArgumentException("Situation non trouvée");

        tableSimulation.changerAction(dtoSituation.getSituationModele(), indexAction);
        deselectionnerActionsSuivantes(dtoSituation, indexAction);

        reconstruireSituations(indexVueSituation + 1);
        selectionnerSituation(indexVueSituation + 1);
        actualiserRange(indexAction);

        selectionnerActionDansVue(dtoSituation, indexAction);
    }

    public void clickSituation(DTOSituation dtoSituation) {
        System.out.println("CLICK SITUATION");
        int indexVueSituation = situations.indexOf(dtoSituation);
        if (indexVueSituation == -1) throw new IllegalArgumentException("Situation non trouvée");
        selectionnerSituation(indexVueSituation);
        actualiserRange(null);
    }

    @Deprecated
    public void clickBoard() {

    }

    public void clickCombo(String nomCombo) {
        System.out.println("Combo clické : " + nomCombo);
        actualiserVueCombo(nomCombo);
    }

    /**
     * méthode lancée quand on sélectionne un format sélection
     * on se fait pas chier, on reconstruit toute la série d'actions
     * car va être galère si pas d'équivalence
     * todo : on pourrait faire la même série d'action si elle existe
     */
    public void formatSelectionne(FormatSolution formatSolution) {
        System.out.println("FORMAT SELECTIONNE");
        // todo que faire si on a aucun formatSolution
        tableSimulation.setFormatSolution(formatSolution);
        infosSolution.setVariante(formatSolution.getNomFormat().name());
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
            tableSimulation.setStack(joueurModele, joueurDepart.getStack());
            tableSimulation.setHero(joueurModele, joueurDepart.getHero());
            tableSimulation.setBounty(joueurModele, joueurDepart.getBounty());
        }
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
        actualiserRange(null);
    }

    /**
     * on vérifie que toutes les situations précédentes ont une action sélectionnée
     * sinon par défaut
     * on doit actualiser TablePoker ET VueTable
     */
    private void etatParDefautSituationPrecedente(int indexVueSituation) {
        for (int i = 0; i < indexVueSituation; i++) {
            DTOSituation dtoSituation = situations.get(i);
            if (dtoSituation == null) throw new RuntimeException("Aucun DTO trouvé");
            Integer indexAction = tableSimulation.fixerActionParDefaut(dtoSituation.getSituationModele());
            // si c'est pas nul, ça veut dire que l'action n'était pas fixée
            if (indexAction != null) {
                selectionnerActionDansVue(dtoSituation, indexAction);
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

        // cas où il n'y a pas d'action suivante on ne fait rien
        if (indexVueSituation == 0) {
            for (DTOSituation situationSupprimee : situations) {
                vueTable.supprimerSituation(situationSupprimee);
            }
            situations.clear();
            situationModele = null;
        }

        else if (indexVueSituation >= situations.size()) {
            return;
        }

        else {
            DTOSituation dtoSituation = situations.get(indexVueSituation);
            if (dtoSituation == null) {
                situationModele = null;
            } else {
                situationModele = dtoSituation.getSituationModele();
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
            TablePoker.JoueurTable joueurSimulation = nouvelleSituation.getJoueur();
            DTOJoueur dtoJoueur = mappageJoueurs.get(joueurSimulation);
            // si le mappage du joueur n'existe pas, il y a une erreur quelque part
            if (dtoJoueur == null) {
                throw new RuntimeException("Joueur non trouvé");
            }
            DTOSituation nouvelleCase =
                    new DTOSituation(nouvelleSituation, dtoJoueur,
                            nouvelleSituation.getStack());
            // on ajoute les actions possibles dans la situation
            for (SimuAction simuAction : nouvelleSituation.getActions()) {
                nouvelleCase.ajouterAction(simuAction.getMove(), simuAction.getBetSize(), simuAction.getIndex());
            }

            situations.add(nouvelleCase);
            vueTable.ajouterSituation(nouvelleCase);
            System.out.println("SITUATION AJOUTEE PAR CONTROLEUR : " + nouvelleCase);
            System.out.println("SIMU SITUATION : " + nouvelleSituation);
        }
    }

    // méthodes de contrôle de la vue

    private void selectionnerSituation(int indexVueSituation) {
        DTOSituation situation;
        try {
            situation = situations.get(indexVueSituation);
        }
        // parfois la situation suivante n'existe pas car on est sur une leaf
        catch (IndexOutOfBoundsException e) {
            indexVueSituation--;
            situation = situations.get(indexVueSituation);
        }

        // on sélectionne un état par défaut si il n'y en a pas pour les situations antérieures
        etatParDefautSituationPrecedente(indexVueSituation);
        // on informe la vue que cette situation est sélectionnée
        vueTable.selectionnerSituation(situation);
        tableSimulation.setSituationSelectionnee(situation.getSituationModele());
    }

    /**
     * méthode utilisée pour afficher la range
     * @param indexAction => vaut null si pas d'action sélectionnée => dans ce cas on affiche toutes les ranges
     */
    private void actualiserRange(Integer indexAction) {
        // la vue ne conserve pas la mémoire des ranges, seulement TablePoker donc on redemande à chaque fois
        LinkedHashMap<SimuAction, RangeIso> ranges = tableSimulation.getRanges(indexAction);
        rangeVisible.reset();
        for (SimuAction simuAction : ranges.keySet()) {
            int rangAction = rangeVisible.ajouterAction(simuAction.getMove(), simuAction.getBetSize());
            RangeIso rangeAction = (RangeIso) simuAction.getRange();
            if (rangeAction == null) {
                vueTable.viderRange();
                return;
            }
            for (ComboIso comboIso : rangeAction.getCombos()) {
                rangeVisible.ajouterValeurCombo(
                        rangAction, comboIso.codeReduit(), comboIso.getValeur(), comboIso.getNombreCombos());
            }
        }
        actualiserVueCombo(null);
        vueTable.actualiserVueRange();
    }

    private void actualiserVueCombo(String nomCombo) {
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

    }

    /**
     * pour éviter la gestion trop complexe, c'est le contrôleur qui dit à la vue de
     * mettre les actions comme sélectionnées
     */
    private void selectionnerActionDansVue(DTOSituation dtoSituation, int indexAction) {
        dtoSituation.setActionSelectionnee(indexAction);
        vueTable.selectionnerAction(dtoSituation, indexAction);
    }

    private void deselectionnerActionsSuivantes(DTOSituation dtoSituation, int indexAction) {
        dtoSituation.deselectionnerAction();
        vueTable.deselectionnerAction(dtoSituation, indexAction);
    }

    @Override
    public void demarrer() {
        // todo : mettre le dernier format utilisé

        // todo créer la vue de départ
    }

    @Override
    public void lancerVue() {
        vueTable.setVisible(true);
    }

    @Override
    public void desactiverVue() {
        vueTable.setVisible(false);
    }
}
