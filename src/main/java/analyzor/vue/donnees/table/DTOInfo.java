package analyzor.vue.donnees.table;

import analyzor.vue.donnees.table.DTOSituation;

public class DTOInfo implements DTOSituation, DTOSituationErreur {
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
