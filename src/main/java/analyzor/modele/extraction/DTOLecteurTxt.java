package analyzor.modele.extraction;

import analyzor.modele.parties.Action;
import analyzor.modele.poker.ComboReel;

import java.lang.reflect.Type;

/**
 * transmission des données entre RegexMatcher et Lecteur
 * générique pour tous les lecteurs txt
 */
public class DTOLecteurTxt {

    public static class SituationJoueur {
        private final String nomJoueur;
        private final float stack;
        private final float bounty;
        private final int siege;

        public SituationJoueur(String playName, int seat, float stack, float bounty) {
            this.nomJoueur = playName;
            this.siege = seat;
            this.stack = stack;
            this.bounty = bounty;
        }

        public String getNomJoueur() {
            return nomJoueur;
        }

        public int getSiege() {
            return siege;
        }

        public float getStack() {
            return stack;
        }

        public float getBounty() {
            return bounty;
        }

        public boolean hasBounty() {
            // bounty vaudra plus de 0 si existe
            return bounty > 0;
        }
    }

    public static class DetailAction {
        private final String nomJoueur;
        private final Action action;
        private final boolean betTotal;
        private final boolean betComplet;


        public DetailAction(String playName, Action action, boolean totalBet, boolean betComplet) {
            this.nomJoueur = playName;
            this.action = action;
            this.betTotal = totalBet;
            this.betComplet = betComplet;
        }

        public Action getAction() {
            return action;
        }

        public String getNomJoueur() {
            return nomJoueur;
        }

        public boolean getBetTotal() {
            return betTotal;
        }

        public boolean getBetComplet() {
            return betComplet;
        }
    }

    public static class DetailGain {
        private final String nomJoueur;
        private final float gains;
        private final ComboReel combo;

        public DetailGain(String nomJoueur, float gains, ComboReel comboJoueur) {
            this.nomJoueur = nomJoueur;
            this.gains = gains;
            this.combo = comboJoueur;
        }

        public String getNomJoueur() {
            return nomJoueur;
        }
        public float getGains() {
            return gains;
        }

        public boolean cartesTrouvees() {
            return combo != null;
        }

        public ComboReel getCombo() {
            return combo;
        }
    }

    @Deprecated
    public static class StructureBlinde {
        private String nomJoueurBB;
        private String nomJoueurSB;

        public String getJoueurBB() {
            return nomJoueurBB;
        }

        public String getJoueurSB() {
            return nomJoueurSB;
        }

        public void setJoueurBB(String playName) {
            this.nomJoueurBB = playName;
        }

        public void setJoueurSB(String playName) {
            this.nomJoueurSB = playName;
        }
    }

    public static class BlindesAnte {
        public enum TypeTaxe {
            BLINDES, ANTE
        }
        private final String nomJoueur;
        private final TypeTaxe typeTaxe;
        private final float valeur;

        public BlindesAnte(String nomJoueur, TypeTaxe typeTaxe, float valeur) {
            if (typeTaxe == null) throw new IllegalArgumentException("Type de taxe non définie");

            this.nomJoueur = nomJoueur;
            this.valeur = valeur;
            this.typeTaxe = typeTaxe;
        }

        public String getNomJoueur() {
            return nomJoueur;
        }

        public float getValeur() {
            return valeur;
        }

        public boolean estBlinde() {
            return typeTaxe == TypeTaxe.BLINDES;
        }

        public boolean estAnte() {
            return typeTaxe == TypeTaxe.ANTE;
        }
    }
}
