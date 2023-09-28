package analyzor.modele.extraction;

import analyzor.controleur.ProgressionTache;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class GestionnaireRoom implements ControleGestionnaire {
    /*
    garde la liste des fichiers et des dossiers adaptés à chaque room
    les instances particulières choissisent le bon Lecteur en fonction de procédures internes
     */
    static private final String dossierSauvegarde = "sauvegarde";
    static private final String nomFichiers = "fichiers.txt";
    static private final String nomDossiers = "dossiers.txt";
    static private final String nomLogs = "logs.txt";
    protected String nomRoom;
    protected String detailRoom;
    protected FileHandler fileHandler;
    private List<Path> cheminsFichiers = new ArrayList<>();
    private List<Path> cheminsDossiers = new ArrayList<>();
    protected int nombreMains = 0;
    //todo si un seul lecteur par Room on pourrait mettre le lecteur ici (mêmes méthodes grâce à l'interface)
    protected GestionnaireRoom(String nomRoom, String nomDetailRoom) {
        this.nomRoom = nomRoom;
        this.detailRoom = nomDetailRoom;
        configurerFileHandler();

    }
    private void configurerFileHandler() {
        // on crée le logger pour journalisation à transmettre aux différentes classes
        try {
            Path pathLog = Paths.get(dossierSauvegarde, nomRoom, nomLogs);
            this.fileHandler = new FileHandler(pathLog.toString());
            this.fileHandler.setFormatter(new SimpleFormatter());

        }
        catch (Exception ignored){
            //todo obligatoire de gérer l'exception que faire ?
        }
    }

    // va chercher tout seul les noms de dossiers
    public abstract boolean autoDetection();

    public ProgressionTache importer() {
        // va importer tous les fichiers des dossiers qui existent
        // on vérifie à chaque loop que progression tâche est toujours actif et on l'incrémente
        return null;
    }

    public boolean ajouterDossier(Path cheminDuDossier) {
        // on va tester un nom de fichier pour voir s'il est importable
        return false;
    }

    public boolean supprimerDossier(String cheminDuDossier) {
        return false;
    }

    public abstract boolean ajouterFichier(Path cheminDuFichier);


    private void fichierAjoute(Path cheminDuFichier) {
        // enregistre le nom du fichier comme déjà traité

    }
    private void dossierAjoute(Path cheminDuDossier) {

    }

    public String getNomRoom(){
        return nomRoom;
    }
    public String getDetailRoom() {
        return detailRoom;
    }
    public boolean getConfiguration() {
        return cheminsFichiers.size() > 0;
    }
    public int nombreDossiers() {
        return cheminsDossiers.size();
    }

    public int nombreFichiers() {
        return cheminsFichiers.size();
    }

    public int nombreMains() {
        return nombreMains;
    }

    public String[] getDossiers() {
        String[] stringDossiers = new String[cheminsDossiers.size()];
        for (int i = 0; i < cheminsDossiers.size(); i++) {
            stringDossiers[i] = cheminsDossiers.get(i).toString();
        }
        return stringDossiers;
    }

    public int fichiersParDossier(String nomDossier) {
        //todo : comment faire? parcourir le dossier ou bien le garder en mémoire
        return 0;
    }

}
