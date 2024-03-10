package analyzor.vue.donnees.format;

import java.time.format.DateTimeFormatter;

/**
 * consultation détaillé d'un format
 * ne permet de modifier que le nom
 */
public class FormConsultationFormat extends FormFormat {
    public FormConsultationFormat(DTOFormat formatConsulte) {
        this.format = formatConsulte;
    }

    public DTOFormat getFormat() {
        return format;
    }

    // changer valeur

    public void changerNom(String nouveauNom) {
        this.format.setNom(nouveauNom);
    }

    // valeur des infos

    public String getNomFormat() {
        return format.getPokerFormat().toString();
    }

    public String getMinAnte() {
        return String.valueOf(format.getAnteMin());
    }

    public String getMaxAnte() {
        return String.valueOf(format.getAnteMax());
    }


    public String getMinRake() {
        return String.valueOf(format.getRakeMin());
    }

    public String getMaxRake() {
        return String.valueOf(format.getRakeMax());
    }

    public String getBounty() {
        if (format.getBounty()) {
            return "oui";
        }
        else return "non";
    }

    public String getNombreJoueurs() {
        return String.valueOf(format.getnJoueurs());
    }

    public String getMinBuyIn() {
        return String.valueOf(format.getMinBuyIn());
    }

    public String getMaxBuyIn() {
        return String.valueOf(format.getMaxBuyIn());
    }

    public String getDateCreation() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        return format.getDateCreation().format(formatter);
    }

    public boolean calculPossible() {
        return !format.isPreflopCalcule() && format.getNombreParties() > 0;
    }

    public String getNombreParties() {
        return String.valueOf(format.getNombreParties());
    }
}
