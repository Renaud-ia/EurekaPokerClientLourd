package analyzor.controleur;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.simulation.JoueurSimulation;
import analyzor.modele.simulation.SimuAction;
import analyzor.modele.simulation.SimuSituation;
import analyzor.modele.simulation.TablePoker;
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
    private final TablePoker tablePoker;
    private final VueTable vueTable;
    private final FenetreConfiguration fenetreConfiguration;
    private final InfosSolution infosSolution;
    private final ConfigTable configTable;
    // stocke l'ordre d'affichage des actions
    private final LinkedList<DTOSituation> situations;
    private final HashMap<JoueurSimulation, DTOJoueur> mappageJoueurs;
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
        fenetrePrincipale.add(vueTable);
        fenetrePrincipale.repaint();
        fenetrePrincipale.revalidate();

        fenetreConfiguration = new FenetreConfiguration(fenetrePrincipale, this, configTable);
        tablePoker = new TablePoker();
    }

    // méthodes publiques correspondant aux différents interactions de l'user

    public void clickSolution() {
        controleurPrincipal.gererFormats();
    }

    public void clickGestionTable() {
        fenetreConfiguration.afficher();
    }

    // interface utilisée par la vue pour signifier qu'on a clické sur une action
    public void clickAction(int indexVueSituation, int indexAction) {
        DTOSituation dtoSituation = situations.get(indexVueSituation);
        if (dtoSituation == null) {
            throw new IllegalArgumentException("DTO situation non trouve");
        }

        tablePoker.changerAction(dtoSituation.getSituationModele(), indexAction);
        selectionnerActionDansVue(dtoSituation, indexAction);
        deselectionnerActionsSuivantes(dtoSituation, indexAction);

        reconstruireSituations(indexVueSituation + 1);
        selectionnerSituation(indexVueSituation + 1);
        actualiserRange(indexAction);

    }

    public void clickSituation(int indexVueSituation) {
        selectionnerSituation(indexVueSituation);
        actualiserRange(null);
    }

    @Deprecated
    public void clickBoard() {

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
        // todo que faire si on a aucun formatSolution
        tablePoker.setFormatSolution(formatSolution);
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
            JoueurSimulation joueurModele = joueurDepart.getJoueurModele();
            tablePoker.setStack(joueurModele, joueurDepart.getStack());
            tablePoker.setHero(joueurModele, joueurDepart.getHero());
            tablePoker.setBounty(joueurModele, joueurDepart.getBounty());
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
        for (JoueurSimulation joueurSimulation : tablePoker.getJoueurs()) {
            DTOJoueur dtoJoueur = new DTOJoueur(joueurSimulation, joueurSimulation.getNomPosition(),
                    joueurSimulation.estHero(), joueurSimulation.getBounty(), joueurSimulation.getStackDepart());
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
            Integer indexAction = tablePoker.fixerActionParDefaut(dtoSituation.getSituationModele());
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
        DTOSituation dtoSituation = situations.get(indexVueSituation);
        SimuSituation situationModele;
        if (dtoSituation == null) {
            situationModele = null;
        }
        else {
            situationModele = dtoSituation.getSituationModele();
        }

        // on supprime toutes les situations qui suivent
        int dernierIndex = situations.size();
        List<DTOSituation> situationsSupprimees = situations.subList(indexVueSituation, dernierIndex);
        for (DTOSituation situationSupprimee : situationsSupprimees) {
            vueTable.supprimerSituation(situationSupprimee);
        }
        situationsSupprimees.clear();

        // on ajoute les nouvelle situations
        LinkedList<SimuSituation> situationsChangees = tablePoker.situationsSuivantes(situationModele);
        for (SimuSituation nouvelleSituation : situationsChangees) {
            JoueurSimulation joueurSimulation = nouvelleSituation.getJoueur();
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
                nouvelleCase.ajouterAction(simuAction.getNom(), simuAction.getBetSize(), simuAction.getIndex());
            }

            situations.add(nouvelleCase);
            vueTable.ajouterSituation(nouvelleCase);
        }
    }

    // méthodes de contrôle de la vue

    private void selectionnerSituation(int indexVueSituation) {
        DTOSituation situation = situations.get(indexVueSituation);
        // parfois la situation suivante n'existe pas car on est sur une leaf
        if (situation == null) {
            indexVueSituation--;
            situation = situations.get(indexVueSituation);
        }
        if (situation == null) {
            throw new IllegalArgumentException("Aucun DTO trouvé correspondant");
        }

        // on sélectionne un état par défaut si il n'y en a pas pour les situations antérieures
        etatParDefautSituationPrecedente(indexVueSituation);
        // on informe la vue que cette situation est sélectionnée
        vueTable.selectionnerSituation(situation);
        tablePoker.setSituationSelectionnee(situation.getSituationModele());
    }

    /**
     * méthode utilisée pour afficher la range
     * @param indexAction => vaut null si pas d'action sélectionnée => dans ce cas on affiche toutes les ranges
     */
    private void actualiserRange(Integer indexAction) {
        // la vue ne conserve pas la mémoire des ranges, seulement TablePoker donc on redemande à chaque fois
        LinkedHashMap<SimuAction, RangeIso> ranges = tablePoker.getRanges(indexAction);
        rangeVisible.reset();
        for (SimuAction simuAction : ranges.keySet()) {
            int rangAction = rangeVisible.ajouterAction(simuAction.getNom(), simuAction.getBetSize());
            RangeIso rangeAction = ranges.get(simuAction);
            for (ComboIso comboIso : rangeAction.getCombos()) {
                rangeVisible.ajouterValeurCombo(rangAction, comboIso.codeReduit(), comboIso.getValeur());
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

        if (rangeVisible.equiteInconnue()) {
            float equite = tablePoker.getEquite(nomCombo);
            rangeVisible.setEquite(equite);
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
