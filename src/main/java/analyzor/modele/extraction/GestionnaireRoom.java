package analyzor.modele.extraction;

import analyzor.controleur.ProgressionTache;
import analyzor.controleur.WorkerAffichable;
import analyzor.modele.exceptions.ErreurInterne;
import analyzor.modele.logging.GestionnaireLog;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.RequetesBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

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
    protected FileHandler fileHandler;
    private List<String> cheminsFichiers = new ArrayList<>();
    private List<Path> cheminsDossiers = new ArrayList<>();
    protected int nombreMains = 0;
    protected Logger logger;
    private final PokerRoom room;
    private Map<Path, Integer> nFichiersDossier = new HashMap<Path, Integer>();

    //todo si un seul lecteur par Room on pourrait mettre le lecteur ici (mêmes méthodes grâce à l'interface)
    protected GestionnaireRoom(PokerRoom room) {
        this.room = room;
        this.nomRoom = room.toString();
        this.logger = GestionnaireLog.getLogger("Gestionnaire" + nomRoom);
        GestionnaireLog.setHandler(logger, GestionnaireLog.importMains);
        recupererChemins();
    }

    private void recupererChemins() {
        // à l'initialisation récupère tous les dossiers et fichiers
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        CriteriaBuilder cbDossiers = session.getCriteriaBuilder();
        CriteriaQuery<DossierImport> queryDossiers = cbDossiers.createQuery(DossierImport.class);
        Root<DossierImport> rootDossier = queryDossiers.from(DossierImport.class);
        queryDossiers.where(cbDossiers.equal(rootDossier.get("room"), this.room));
        List<DossierImport> dossiers = session.createQuery(queryDossiers).getResultList();

        for (DossierImport dossier : dossiers) {
            if (dossier.actif) {
                cheminsDossiers.add(dossier.getChemin());
                nFichiersDossier.put(dossier.getChemin(), dossier.getnFichiersImportes());
            }
        }

        CriteriaBuilder cbFichiers = session.getCriteriaBuilder();
        CriteriaQuery<FichierImport> queryFichiers = cbFichiers.createQuery(FichierImport.class);
        Root<FichierImport> rootFichier = queryFichiers.from(FichierImport.class);
        queryFichiers.where(cbFichiers.equal(rootFichier.get("room"), this.room));
        List<FichierImport> fichiers = session.createQuery(queryFichiers).getResultList();

        for (FichierImport fichier : fichiers) {
                // pas de map!!! => risque de doublons si fichier pas au même endroit
            cheminsFichiers.add(fichier.getNom());
        }

        RequetesBDD.fermerSession();
        logger.fine("Chemins récupérés dans BDD");
    }

    // va chercher tout seul les noms de dossiers
    public abstract boolean autoDetection();

    public WorkerAffichable importer() {
        // va importer tous les fichiers des dossiers qui existent

        // on construit d'abord la liste des fichiers à importer
        List<Path> nouveauxFichiers = new ArrayList<>();
        int compteFichiers = listerNouveauxFichiers(nouveauxFichiers);
        if (nouveauxFichiers.size() == 0) return null;

        WorkerAffichable worker = new WorkerAffichable("Importer " + nomRoom, compteFichiers) {
            @Override
            protected Void executerTache() {
                int i = 0;
                for (Path dossierExistant : cheminsDossiers) {
                    if (isCancelled()) {
                        gestionInterruption();
                        return null;
                    }
                    try {
                        for (Path cheminFichier : nouveauxFichiers) {
                            ajouterFichier(cheminFichier);
                            publish(i++);
                            fichierAjoute(cheminFichier);
                        }

                    } catch (Exception e) {
                        //log pas sensible
                        //on continue le traitement
                        logger.log(Level.WARNING, "Impossible d'ajouter le fichier", e);
                    }
                }
                return null;
            }
        };

        logger.info("Worker créé pour import : " + nomRoom);

        return worker;
    }

    private int listerNouveauxFichiers(List<Path> nouveauxFichiers) {
        //todo => pour test à supprimer
        int MAX_FICHIERS = 10;

        int compteFichiers = 0;
        for (Path dossierExistant : cheminsDossiers) {
            try (Stream<Path> stream = Files.walk(dossierExistant)) {
                Iterator<Path> iterator = stream.iterator();
                while (iterator.hasNext() && compteFichiers < MAX_FICHIERS) {
                    Path currentPath = iterator.next();

                    if (Files.isRegularFile(currentPath)) {
                        String nomFichier = currentPath.getFileName().toString();
                        if (!cheminsFichiers.contains(nomFichier)) {
                            nouveauxFichiers.add(currentPath);
                            compteFichiers++;

                            //on compte les fichiers avant qu'ils soient effectivement ajoutés
                            //pas trop le choix mais pas très grave
                            nFichiersDossier.merge(dossierExistant, 1, Integer::sum);
                            cheminsFichiers.add(nomFichier);
                        }
                    }
                }
            }
            catch (IOException e) {
                //log pas sensible
                //on continue le traitement
                logger.log(Level.WARNING, "Impossible de lire le fichier", e);
            }
        }
        return compteFichiers;
    }

    public boolean ajouterDossier(Path cheminDuDossier) {
        //il faudrait vérifier si le dossier n'est pas un sous dossier qui existe déjà
        for (Path dossierExistant : cheminsDossiers) {
            if (cheminDuDossier.startsWith(dossierExistant)) {
                logger.info(cheminDuDossier.toString() + " est un sous-dossier de " + dossierExistant);
                return false;
            }
        }

        final int MAX_DEPTH = 2;
        final int FICHIERS_TESTES = 3;

        try (Stream<Path> stream = Files.walk(cheminDuDossier, MAX_DEPTH)) {
            stream.filter(Files::isRegularFile)
                    .limit(FICHIERS_TESTES)
                    .forEach(this::fichierEstValide);
        }
        catch (IOException e) {
            //log pas sensible
            logger.log(Level.WARNING, "Impossible d'ajouter le dossier", e);
            return false;
        }

        if (dossierAjoute(cheminDuDossier)) {
            logger.fine("Le dossier a bien été ajouté : " + cheminDuDossier);
            return true;
        }
        else return false;
    }

    public boolean supprimerDossier(String cheminDuDossier) {
        //on désactive dans base
        //on supprime de notre liste
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        CriteriaBuilder cbDossiers = session.getCriteriaBuilder();
        CriteriaQuery<DossierImport> queryDossiers = cbDossiers.createQuery(DossierImport.class);
        Root<DossierImport> rootDossier = queryDossiers.from(DossierImport.class);
        queryDossiers.where(cbDossiers.equal(rootDossier.get("room"), this.room));
        queryDossiers.where(cbDossiers.equal(rootDossier.get("cheminDossier"), cheminDuDossier));
        List<DossierImport> dossiers = session.createQuery(queryDossiers).getResultList();

        if (dossiers.size() != 1) {
            logger.warning("Erreur suppression, plusieurs résultats renvoyés pour : " + cheminDuDossier);
            return false;
        }
        dossiers.get(0).actif = false;
        session.persist(dossiers.get(0));
        RequetesBDD.fermerSession();

        Path pathSuppression = Paths.get(cheminDuDossier);
        if (!cheminsDossiers.remove(pathSuppression)) {
            logger.warning("Erreur suppression, le dossier n'a pu être éliminé de la liste : " + cheminDuDossier);
        }
        return true;
    }

    protected boolean ajouterFichier(Path cheminDuFichier) {
        return !cheminsFichiers.contains(cheminDuFichier.getFileName().toString());
    }

    private void fichierAjoute(Path cheminDuFichier) {
        // rajoute le nom du fichier dans la BDD et dans notre liste
        String nomFichier = cheminDuFichier.getFileName().toString();
        FichierImport fichierImport = new FichierImport(nomFichier);
        RequetesBDD.getOrCreate(fichierImport);

        this.cheminsFichiers.add(nomFichier);
    }
    private boolean dossierAjoute(Path cheminDuDossier) {
        DossierImport dossierStocke = new DossierImport(this.room, cheminDuDossier);
        DossierImport dossierCree = (DossierImport) RequetesBDD.getOrCreate(dossierStocke);

        if (dossierCree == null) {
            logger.severe("Impossible de récupérer le dossier créé ou correspondant");
            return false;
        }

        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        dossierCree.actif = true;
        session.persist(dossierCree);
        RequetesBDD.fermerSession();

        this.cheminsDossiers.add(cheminDuDossier);
        return true;
    }

    public String getNomRoom(){
        return nomRoom;
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

    public Integer fichiersParDossier(String nomDossier) {
        return nFichiersDossier.get(Paths.get(nomDossier));
    }

    protected abstract boolean fichierEstValide(Path cheminDuFichier);
}
