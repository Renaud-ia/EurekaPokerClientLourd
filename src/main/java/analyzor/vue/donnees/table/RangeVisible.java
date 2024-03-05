package analyzor.vue.donnees.table;

import analyzor.modele.parties.Move;
import analyzor.modele.poker.Carte;
import analyzor.vue.basiques.CouleursActions;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

/**
 * classe de stockage de la range affichée
 * convertit les noms de combos en position sur la grille
 * définit les couleurs
 * todo : afficher le message s'il y en a un
 */
public class RangeVisible {
    private final static Character[] RANKS = trierRanks();
    private final HashMap<String, ComboVisible> matriceCombos;
    public final LinkedList<ComboVisible> combosOrdonnes;
    public final LinkedList<ActionVisible> actionsGlobales;
    private ComboVisible comboSelectionne;
    private CouleursActions couleursActions;
    private String message;
    private Integer actionSelectionnee;
    private static String choixFlopZilla = "Flopzilla/GTO+";
    private static String choixPio = "PioSOLVER";

    public RangeVisible() {
        matriceCombos = new HashMap<>();
        combosOrdonnes = new LinkedList<>();
        actionsGlobales = new LinkedList<>();

        // on veut initialiser la range et faire qu'une range vide apparaisse
        reset();
    }

    // interface utilisée pour ajouter les données

    public void reset() {
        matriceCombos.clear();
        combosOrdonnes.clear();
        actionsGlobales.clear();
        comboSelectionne = null;
        couleursActions = new CouleursActions();
        message = null;

        construireMatrice();
    }

    /**
     * méthode pour ajouter des Actions
     * @return
     */
    public int ajouterAction(Move move, float betSize) {
        Color couleurAction;
        if (move == Move.FOLD) {
            couleurAction = CouleursActions.FOLD;
        }
        else if (move == Move.CALL) {
            couleurAction = CouleursActions.CALL;
        }
        else if (move == Move.RAISE) {
            couleurAction = couleursActions.raiseSuivant();
        }
        else if (move == Move.ALL_IN) {
            couleurAction = CouleursActions.ALL_IN;
        }
        else {
            couleurAction = CouleursActions.ACTION_NON_DEFINIE;
        }

        ActionVisible actionGlobale = new ActionVisible(move.toString(), betSize, couleurAction);
        actionsGlobales.add(actionGlobale);

        for (ComboVisible comboVisible : combosOrdonnes) {
            ActionVisible actionCombo = new ActionVisible(move.toString(), betSize, couleurAction);
            comboVisible.ajouterAction(actionCombo);
        }

        return actionsGlobales.size() - 1;
    }

    public void ajouterValeurCombo(int rangAction, String nomCombo, float valeur, int nCombos) {
        ComboVisible comboVisible = matriceCombos.get(nomCombo);
        if (comboVisible == null) throw new IllegalArgumentException("Combo non trouvé");

        // on met la valeur de l'action dans le combo
        comboVisible.setValeurAction(rangAction, valeur);

        // on incrémente les actions globales
        ActionVisible actionGlobale = actionsGlobales.get(rangAction);
        actionGlobale.incrementerPourcentage(valeur * nCombos / 1326);

    }

    // interface utilisée pour sélectionner les données depuis le controleur

    public void setComboSelectionne(String nomCombo) {
        ComboVisible comboVisible = matriceCombos.get(nomCombo);
        if (comboVisible == null) throw new IllegalArgumentException("Combo non trouvé");
        comboSelectionne = comboVisible;
    }

    public void setActionSelectionnee(Integer indexAction) {
        for (ComboVisible comboVisible : combosOrdonnes) {
            comboVisible.setActionSelectionnee(indexAction);
        }
        this.actionSelectionnee = indexAction;
    }


    public String selectionnerComboDefaut() {
        comboSelectionne = combosOrdonnes.get(0);
        return comboSelectionne.getNom();
    }

    public boolean equiteInconnue(String nomCombo) {
        ComboVisible comboVisible = matriceCombos.get(nomCombo);
        if (comboVisible == null) throw new IllegalArgumentException("Combo non trouvé");
        return !(comboVisible.equiteCalculee());
    }

    public void setEquite(String nomCombo, float equite) {
        ComboVisible comboVisible = matriceCombos.get(nomCombo);
        if (comboVisible == null) throw new IllegalArgumentException("Combo non trouvé");
        comboVisible.setEquite(equite);
    }

    public Integer getActionSelectionnee() {
        return actionSelectionnee;
    }


    // utilisé par la vue pour récupérer les infos globales

    /**
     * @return une liste de combos dans l'ordre par ligne et colonne
     */
    public LinkedList<ComboVisible> listeDesCombos() {
        return combosOrdonnes;
    }

    /**
     * @return actions dans l'ordre d'affichage de droite à gauche
     */
    public LinkedList<ActionVisible> actionsGlobales() {
        return actionsGlobales;
    }


    // méthodes privées

    private void construireMatrice() {
        // on veut l'ordre inverse car c'est croissant dans CARTE
        for (int i = 0; i < RANKS.length; i++) {
            Character premierRank = RANKS[i];
            for (int j = 0; j < RANKS.length; j++) {
                Character secondRank = RANKS[j];
                // todo ça fait un peu doublon avec ComboIso
                String nomCombo;
                if (i > j) {
                    nomCombo = secondRank.toString() + premierRank.toString();
                    nomCombo += "o";
                }
                else if (i < j) {
                    nomCombo = premierRank.toString() + secondRank.toString();
                    nomCombo += "s";
                }
                else {
                    nomCombo = secondRank.toString() + premierRank.toString();
                }

                ComboVisible comboVisible = new ComboVisible(nomCombo);
                matriceCombos.put(nomCombo, comboVisible);
                combosOrdonnes.add(comboVisible);
            }
        }

        // on sélectionne par défaut le premier combo
        selectionnerComboDefaut();
    }

    private static Character[] trierRanks() {
        if (Carte.STR_RANKS == null || Carte.STR_RANKS[0] != '2')
            throw new IllegalArgumentException("L'ordre des ranks n'est pas bon");
        Character[] ranksInverse = new Character[Carte.STR_RANKS.length];
        int nouvelIndex = 0;
        for (int i = Carte.STR_RANKS.length - 1; i >= 0; i--) {
            ranksInverse[nouvelIndex++] = Carte.STR_RANKS[i];
        }

        return ranksInverse;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean estVide() {
        return actionsGlobales.isEmpty();
    }

    public ComboVisible comboSelectionne() {
        return comboSelectionne;
    }

    public static String[] getChoixExport() {
        return new String[]{choixFlopZilla, choixPio};
    }

    /**
     * @return la range sous forme de chaine de caractère
     */
    public String chaineCaracteres(String choixExport) {
        if (estVide()) {
            return "La range est vide";
        }

        if (actionSelectionnee == null) {
            return "Sélectionnez une action";
        }

        if (Objects.equals(choixExport, choixFlopZilla)) {
            return rangeFlopZilla();
        }

        else if (Objects.equals(choixExport, choixPio)) {
            return rangePio();
        }

        else return "Choix inconnu";
    }

    private String rangePio() {
        StringBuilder stringRange = new StringBuilder();
        for (int i = 0; i < combosOrdonnes.size(); i++) {
            ComboVisible comboVisible = combosOrdonnes.get(i);
            ActionVisible actionVisible = comboVisible.actions.get(actionSelectionnee);

            if (actionVisible.getPourcentage() == 0) continue;

            if (i > 0) stringRange.append(",");

            stringRange.append(comboVisible.nomCombo);
            stringRange.append(":").append(actionVisible.getPourcentage() / 100);
        }

        return stringRange.toString();
    }

    private String rangeFlopZilla() {
        StringBuilder stringRange = new StringBuilder();
        for (int i = 0; i < combosOrdonnes.size(); i++) {
            ComboVisible comboVisible = combosOrdonnes.get(i);
            ActionVisible actionVisible = comboVisible.actions.get(actionSelectionnee);

            if (actionVisible.getPourcentage() == 0) continue;

            if (i > 0) stringRange.append(",");

            stringRange.append("[").append(actionVisible.getPourcentage()).append("]");
            stringRange.append(comboVisible.nomCombo);
            stringRange.append("[/").append(actionVisible.getPourcentage()).append("]");
        }

        return stringRange.toString();
    }


    // classes internes pour partage des infos

    public class ComboVisible {
        private final String nomCombo;
        private final LinkedList<ActionVisible> actions;
        private Float equite;
        private Integer actionSelectionnee;

        // construction du combo
        private ComboVisible(String nomCombo) {
            this.nomCombo = nomCombo;
            this.actions = new LinkedList<>();
            this.equite = null;
            this.actionSelectionnee = null;
        }

        private void ajouterAction(ActionVisible actionCombo) {
            this.actions.add(actionCombo);
        }

        private boolean equiteCalculee() {
            System.out.println("EQUITE DU COMBO : " + equite);
            return equite != null;
        }

        private void setEquite(float equite) {
            this.equite = equite;
        }


        // récupération des infos par la vue

        public LinkedList<ActionVisible> getActions() {
            LinkedList<ActionVisible> actionVisibles = new LinkedList<>();
            for (int i = 0; i < actions.size(); i++) {
                if (actionSelectionnee == null || actionSelectionnee == i) {
                    actionVisibles.add(actions.get(i));
                }
            }
            return actionVisibles;
        }

        public String getNom() {
            return nomCombo;
        }

        public String getEquite() {
            if (equite == null) return "inconnue";
            else return Math.round(equite * 10000) / 100 + "%";
        }

        public void setValeurAction(int rangAction, float valeur) {
            ActionVisible actionVisible = this.actions.get(rangAction);
            actionVisible.setPourcentage(valeur);
        }

        public void setActionSelectionnee(Integer indexAction) {
            this.actionSelectionnee = indexAction;
        }
    }

    public class ActionVisible {
        private final String nomMove;
        private final float betSize;
        private final Color couleur;
        private float pourcentage;

        // construction
        ActionVisible(String nomMove, float betSize, Color couleurAction) {
            this.nomMove = nomMove;
            this.betSize = betSize;
            this.couleur = couleurAction;
            this.pourcentage = 0;
        }

        void setPourcentage(float valeur) {
            this.pourcentage = valeur;
        }

        void incrementerPourcentage(float valeur) {
            this.pourcentage += valeur;
        }

        // récupération des infos par la vue

        public Color getCouleur(boolean survole) {
            // todo implémenter un changement de couleur si survolé
            return couleur;
        }

        public float getPourcentage() {
            return (float) Math.round(pourcentage * 10000) / 100;
        }

        public String getNom() {
            StringBuilder nomAction = new StringBuilder();
            nomAction.append(nomMove);
            if (betSize > 0) {
                nomAction.append(" ").append(betSize);
            }
            return nomAction.toString();
        }
    }

}
