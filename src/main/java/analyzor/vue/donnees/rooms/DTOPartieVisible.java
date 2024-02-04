package analyzor.vue.donnees.rooms;

/**
 * stockage des infos d'une partie importéée pour affichage
 */
public class DTOPartieVisible {
    private final String cheminFichier;
    private final String statutImport;

    public DTOPartieVisible(String cheminFichier, String statutImport) {
        this.cheminFichier = cheminFichier;
        this.statutImport = statutImport;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public String getStatutImport() {
        return statutImport;
    }
}
