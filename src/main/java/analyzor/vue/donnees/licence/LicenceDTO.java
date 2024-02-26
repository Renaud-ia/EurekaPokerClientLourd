package analyzor.vue.donnees.licence;

public class LicenceDTO {
    private String cleLicence;
    private int statutLicence;

    public void setCleLicence(String cleLicence) {
        this.cleLicence = cleLicence;
    }

    public void setStatutLicence(int statut) {
        this.statutLicence = statut;
    }

    public String getCleLicence() {
        return cleLicence;
    }

    public boolean estActive() {
        return statutLicence == 0;
    }
}
