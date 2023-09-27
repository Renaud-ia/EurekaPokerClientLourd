package analyzor.controleur;

import analyzor.modele.extraction.ControleGestionnaire;
import analyzor.modele.extraction.GestionnaireWinamax;
import analyzor.vue.donnees.InfosRoom;
import analyzor.vue.vues.VuePrincipale;
import analyzor.vue.vues.VueRooms;

import java.nio.file.Path;

public class ControleurRoom {
    //todo : il faut ajouter les gestionnaires qu'on prend en charge ici
    private final ControleGestionnaire[] gestionnaires = {GestionnaireWinamax.obtenir()};
    private final VueRooms vueRooms;
    private int roomSelectionne;
    ControleurRoom(VuePrincipale vuePrincipale) {
        InfosRoom infosRoom = initialiserRooms();
        this.vueRooms = new VueRooms(vuePrincipale, this);
        //vueRooms.afficherRooms(infosRoom);
        bugVue();
        }
    private InfosRoom initialiserRooms() {
        InfosRoom infosRoom = new InfosRoom(gestionnaires.length);
        int index = 0;
        for(ControleGestionnaire gestionnaire : gestionnaires) {
            infosRoom.setRoom(index,
                    gestionnaire.getNomRoom(),
                    gestionnaire.getDetailRoom(),
                    gestionnaire.nombreFichiers(),
                    gestionnaire.nombreMains(),
                    gestionnaire.nombreDossiers(),
                    gestionnaire.getConfiguration()
            );
            String[] nomDossiers = gestionnaire.getDossiers();
            for (String nomDossier : nomDossiers) {
                int nombreFichiers = gestionnaire.fichiersParDossier(nomDossier);
                infosRoom.ajouterDossier(index, nomDossier, nombreFichiers);
            }
            index++;

        }
        return infosRoom;
    }

    public void roomSelectionnee(int index) {
        bugVue();
        //todo il faudrait désactiver le bouton
        if (index == -1) return;
        // on garde ça en mémoire pour modifier la bonne room
        this.roomSelectionne = index;
        this.vueRooms.modifierRoom(index);
    }

    public boolean detection() {
        return gestionnaires[roomSelectionne].autoDetection();
    }

    public boolean ajouterDossier(Path nomDossier) {
        return gestionnaires[roomSelectionne].ajouterDossier(nomDossier);
    }

    public boolean supprimerDossier(String cheminDossier) {
        return gestionnaires[roomSelectionne].supprimerDossier(cheminDossier);
    }

    public int importer() {
        return gestionnaires[roomSelectionne].importer();
    }

    public void bugVue() {
        if (this.vueRooms == null) System.out.println("BUGGGGG");
    }

}
