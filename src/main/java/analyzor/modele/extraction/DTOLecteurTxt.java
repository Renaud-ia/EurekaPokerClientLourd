package analyzor.modele.extraction;

import analyzor.modele.parties.Action;
import analyzor.modele.poker.Combo;
import analyzor.modele.poker.ComboReel;

public class DTOLecteurTxt {
    /*
    transmission des données entre RegexMatcher et Lecteur
     */
    public static class SituationJoueur {
        private final String nomJoueur;
        private final int stack;
        private final float bounty;
        private final int siege;

        public SituationJoueur(String playName, int seat, int stack, float bounty) {
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

        public int getStack() {
            return stack;
        }

        public float getBounty() {
            return bounty;
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
        private final int gains;
        private final ComboReel combo;

        public DetailGain(String nomJoueur, int gains, ComboReel comboJoueur) {
            this.nomJoueur = nomJoueur;
            this.gains = gains;
            this.combo = comboJoueur;
        }

        public String getNomJoueur() {
            return nomJoueur;
        }
        public int getGains() {
            return gains;
        }

        public boolean cartesTrouvees() {
            return getCombo() != null;
        }

        public ComboReel getCombo() {
            return combo;
        }
    }

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
}
