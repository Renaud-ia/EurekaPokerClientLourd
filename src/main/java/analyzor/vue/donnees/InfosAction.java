package analyzor.vue.donnees;

public class InfosAction {
    private static final int MAX_ACTIONS = 6;
    private String position;
    private UniqueAction[] actions = new UniqueAction[MAX_ACTIONS];
    private int current_action = 0;
    public InfosAction() {

    }
    public InfosAction(String position) {
        this.position = position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    public void ajouterAction(String action, float bet_size) {
        actions[current_action++] = new UniqueAction(action, bet_size);
    }

    public String[] getActions() {
        String [] str_actions = new String[current_action];
        for(int i=0;i<current_action; i++) {
            str_actions[i] = actions[i].getAction() + " ";
            str_actions[i] += (actions[i].getBetSize() == 0 ? "" : Float.toString(actions[i].getBetSize()));
        }
        return str_actions;
    }

    class UniqueAction {
        private String action;
        private float bet_size;
        private UniqueAction(String action, float bet_size) {
            this.action = action;
            this.bet_size = bet_size;
        }

        public String getAction() {
            return action;
        }

        public float getBetSize() {
            return bet_size;
        }
    }

}
