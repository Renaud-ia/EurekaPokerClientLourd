package analyzor.vue.donnees;

public class DTOInfo implements DTOSituation {
    private final String message;
    public DTOInfo(String messageInfo) {
        this.message = messageInfo;
    }

    @Override
    public String getNom() {
        return "FIN ACTION";
    }

    public String getMessage() {
        return message;
    }
}
