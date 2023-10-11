package analyzor.modele.extraction;

import analyzor.controleur.WorkerAffichable;
import analyzor.modele.logging.GestionnaireLog;
import analyzor.modele.parties.DataRoom;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.RequetesBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    protected List<String> cheminsFichiers;
    private List<DossierImport> dossierImports;
    protected int nombreMains;
    protected Logger logger;
    private final PokerRoom room;

    //todo si un seul lecteur par Room on pourrait mettre le lecteur ici (mêmes méthodes grâce à l'interface)
    protected GestionnaireRoom(PokerRoom room) {
        this.room = room;
        this.nomRoom = room.toString();
        this.logger = GestionnaireLog.getLogger("Gestionnaire" + nomRoom);
        GestionnaireLog.setHandler(logger, GestionnaireLog.importMains);
        recupererChemins();
    }

    private void recupererChemins() {
        cheminsFichiers = new ArrayList<>();
        dossierImports = new ArrayList<>();
        logger.info("Liste de fichiers et dossiers reset, on va les chercher dans la base de données");
        // à l'initialisation récupère tous les dossiers et fichiers
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        CriteriaBuilder cbDossiers = session.getCriteriaBuilder();
        CriteriaQuery<DossierImport> queryDossiers = cbDossiers.createQuery(DossierImport.class);
        Root<DossierImport> rootDossier = queryDossiers.from(DossierImport.class);
        queryDossiers.where(cbDossiers.equal(rootDossier.get("room"), this.room));
        List<DossierImport> dossiers = session.createQuery(queryDossiers).getResultList();

        //on ajoute les dossiers inactifs, ils ne seront juste pas affichés par controleur
        dossierImports.addAll(dossiers);

        CriteriaBuilder cbFichiers = session.getCriteriaBuilder();
        CriteriaQuery<FichierImport> queryFichiers = cbFichiers.createQuery(FichierImport.class);
        Root<FichierImport> rootFichier = queryFichiers.from(FichierImport.class);
        queryFichiers.where(cbFichiers.equal(rootFichier.get("room"), this.room));
        List<FichierImport> fichiers = session.createQuery(queryFichiers).getResultList();

        for (FichierImport fichier : fichiers) {
                // pas de map!!! => risque de doublons si fichier pas au même endroit
            cheminsFichiers.add(fichier.getNom());
        }

        DataRoom dataRoom = session.get(DataRoom.class, room.ordinal());
        if (dataRoom != null) nombreMains = dataRoom.getNombreMains();
        logger.fine("Nombre de mains récupérés dans BDD : " + nombreMains);

        RequetesBDD.fermerSession();
        logger.fine("Chemins récupérés dans BDD");
    }

    // va chercher tout seul les noms de dossiers
    public abstract boolean autoDetection();

    public WorkerAffichable importer() {
        // va importer tous les fichiers des dossiers qui existent

        // on construit d'abord la liste des fichiers à importer
        List<Path> nouveauxFichiers = new ArrayList<>();
        listerNouveauxFichiers(nouveauxFichiers);
        if (nouveauxFichiers.size() == 0) return null;

        WorkerAffichable worker = new WorkerAffichable("Importer " + nomRoom, nouveauxFichiers.size()) {
            @Override
            protected Void executerTache() {
                int mainsAjouteesTotal = 0;
                int i = 0;
                for (Path cheminFichier : nouveauxFichiers) {
                    logger.info("Traitement dans le worker : " + cheminFichier);
                    if (isCancelled()) {
                        System.out.println("Processus arrêté");
                        gestionInterruption();
                        //on veut quand même ajouter le nombre de mains
                        break;
                    }
                    try {
                        //todo ouvrir la connexion ici pour tout commit d'un coup???
                        Integer ajoutes = (ajouterFichier(cheminFichier));
                        if (ajoutes != null) {
                            fichierAjoute(cheminFichier);
                            mainsAjouteesTotal += ajoutes;
                        }
                        else {
                            logger.warning("Fichier non ajouté");
                        }
                        publish(++i);
                    }
                    catch (Exception e) {
                        //log pas sensible
                        //on continue le traitement
                        logger.log(Level.WARNING, "Impossible d'ajouter le fichier", e);
                        logger.warning(Arrays.toString(e.getStackTrace()));
                        e.printStackTrace();
                        gestionInterruption();
                        //on veut quand même ajouter le nombre de mains
                        break;
                        //todo : on pourrait capturer les exceptions ici, continuer le traitement sauf si trop d'erreurs
                    }
                }
                RequetesBDD.ouvrirSession();
                Session session = RequetesBDD.getSession();
                Transaction transaction = session.beginTransaction();
                DataRoom dataRoom = new DataRoom(room);
                session.merge(dataRoom);
                dataRoom.addNombreMains(mainsAjouteesTotal);
                session.merge(dataRoom);
                transaction.commit();
                RequetesBDD.fermerSession();

                return null;
            }
        };

        logger.info("Worker créé pour import : " + nomRoom);

        return worker;
    }

    private void listerNouveauxFichiers(List<Path> nouveauxFichiers) {
        int compteFichiers = 0;

        for (DossierImport dossierCourant : dossierImports) {
            Path dossierExistant = dossierCourant.getChemin();
            try (Stream<Path> stream = Files.walk(dossierExistant)) {
                Iterator<Path> iterator = stream.iterator();
                while (iterator.hasNext()) {
                    Path currentPath = iterator.next();
                    if (Files.isRegularFile(currentPath)) {
                        String nomFichier = currentPath.getFileName().toString();
                        if (!cheminsFichiers.contains(nomFichier) && fichierEstValide(currentPath)) {
                            logger.info("Dossier ajouté à la liste de traitement");
                            nouveauxFichiers.add(currentPath);
                            compteFichiers++;

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
    }

    public boolean ajouterDossier(Path cheminDuDossier) {
        boolean existant = false;
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        Transaction transaction = session.beginTransaction();

        for (DossierImport dossierCourant : dossierImports) {
            Path dossierExistant = dossierCourant.getChemin();
            if (cheminDuDossier.toString().equals(dossierExistant.toString())) {
                logger.info("Dossier déjà trouvé");
                dossierCourant.actif = true;
                existant = true;
                session.merge(dossierCourant);
                break;
            }

            else if (cheminDuDossier.startsWith(dossierExistant)) {
                logger.info(cheminDuDossier.toString() + " est un sous-dossier de " + dossierExistant);
                transaction.rollback();
                RequetesBDD.fermerSession();
                return false;
            }
        }

        if (!existant) {
            if (dossierEstValide(cheminDuDossier)) {
                DossierImport dossierStocke = new DossierImport(this.room, cheminDuDossier);
                dossierStocke.actif = true;
                this.dossierImports.add(dossierStocke);
                session.merge(dossierStocke);
            }
            else {
                logger.info("Le dossier n'est pas valide");
                transaction.rollback();
                RequetesBDD.fermerSession();
                return false;
            }
        }
        transaction.commit();
        RequetesBDD.fermerSession();
        return true;

    }

    public boolean supprimerDossier(String cheminDuDossier) {
        //on le désactive simplement

        for (DossierImport dossierCourant : dossierImports) {
            Path dossierExistant = dossierCourant.getChemin();
            if (cheminDuDossier.equals(dossierExistant.toString())) {
                logger.info("Dossier trouvé");
                RequetesBDD.ouvrirSession();
                Session session = RequetesBDD.getSession();
                Transaction transaction = session.beginTransaction();
                session.merge(dossierCourant);
                dossierCourant.actif = false;
                transaction.commit();
                RequetesBDD.fermerSession();
                return true;
            }
        }

        logger.warning("Dossier à supprimer non trouvé");
        return false;
    }

    protected abstract Integer ajouterFichier(Path cheminDuFichier);

    private void fichierAjoute(Path cheminDuFichier) {
        // rajoute le nom du fichier dans la BDD et dans notre liste
        String nomFichier = cheminDuFichier.getFileName().toString();

        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        Transaction transaction = session.beginTransaction();

        for (DossierImport dossier: dossierImports) {
            if (cheminDuFichier.toString().contains(dossier.getChemin().toString())) {
                session.merge(dossier);
                dossier.fichierAjoute();
            }
        }

        FichierImport fichierImport = new FichierImport(nomFichier, this.room);
        session.merge(fichierImport);
        transaction.commit();
        RequetesBDD.fermerSession();
        RequetesBDD.getOrCreate(fichierImport);

        this.cheminsFichiers.add(nomFichier);
    }
    private boolean dossierEstValide(Path cheminDuDossier) {
        final int MAX_DEPTH = 4;
        final int FICHIERS_TESTES = 3;

        boolean auMoinsUnFichierEstValide;

        try (Stream<Path> stream = Files.walk(cheminDuDossier, MAX_DEPTH)) {
            auMoinsUnFichierEstValide = stream.filter(Files::isRegularFile)
                    .limit(FICHIERS_TESTES)
                    .anyMatch(this::fichierEstValide);
        }
        catch (IOException e) {
            //log pas sensible
            logger.log(Level.WARNING, "Impossible de parcourir le dossier", e);
            return false;
        }

        return auMoinsUnFichierEstValide;
    }

    private void mergeWithDatabase(Session session) {
        for (DossierImport dossier: dossierImports) {
            session.merge(dossier);
        }
    }

    public String getNomRoom(){
        return nomRoom;
    }
    public boolean getConfiguration() {
        for (DossierImport dossier : dossierImports) {
            if (dossier.actif && dossier.nFichiersImportes > 0) return true;
        }
        return false;
    }
    public int nombreDossiers() {
        return dossierImports.size();
    }

    public int nombreFichiers() {
        return cheminsFichiers.size();
    }

    public int nombreMains() {
        return nombreMains;
    }

    public List<DossierImport> getDossiers() {
        return dossierImports;
    }


    protected abstract boolean fichierEstValide(Path cheminDuFichier);
}
