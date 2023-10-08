package analyzor.modele.extraction;

import analyzor.modele.parties.Action;
import analyzor.modele.poker.Combo;
import analyzor.modele.poker.ComboReel;

public class DTOLecteurTxt {
    public static class SituationJoueur {
        private String nomJoueur;
        private int stack;
        private float bounty;
        private int siege;
        public SituationJoueur() {}

        public SituationJoueur(String playName, int seat, int stack, float bounty) {
            this.nomJoueur = playName;
            this.siege = seat;
            this.stack = stack;
            this.bounty = bounty;
        }

        protected void setNomJoueur(String nomJoueur) {
            this.nomJoueur = nomJoueur;
        }

        protected void setBounty(float bounty) {
            this.bounty = bounty;
        }

        protected void setStack(int stack) {
            this.stack = stack;
        }

        protected void setSiege(int siege) {
            this.siege = siege;
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
        protected String nomJoueur;
        private Action action;
        private boolean betTotal;
        private boolean betComplet;

        public DetailAction() {}

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
        private String nomJoueur;
        private int gains;
        private ComboReel combo;

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
