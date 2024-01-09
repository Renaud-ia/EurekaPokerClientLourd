package analyzor.modele.extraction;

import analyzor.controleur.WorkerAffichable;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.parties.DataRoom;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.bdd.ConnexionBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class GestionnaireRoom implements ControleGestionnaire {
    /*
    garde la liste des fichiers et des dossiers adaptés à chaque room
    les instances particulières choissisent le bon Lecteur en fonction de procédures internes
     */
    protected String nomRoom;
    protected List<String> cheminsFichiers;
    private List<DossierImport> dossierImports;
    protected int nombreMains;
    protected Logger logger = LogManager.getLogger(GestionnaireRoom.class);
    private final PokerRoom room;

    //todo si un seul lecteur par Room on pourrait mettre le lecteur ici (mêmes méthodes grâce à l'interface)
    protected GestionnaireRoom(PokerRoom room) {
        this.room = room;
        this.nomRoom = room.toString();
        recupererChemins();
    }

    private void recupererChemins() {
        cheminsFichiers = new ArrayList<>();
        dossierImports = new ArrayList<>();
        logger.info("Liste de fichiers et dossiers reset, on va les chercher dans la base de données");
        // à l'initialisation récupère tous les dossiers et fichiers
        Session session = ConnexionBDD.ouvrirSession();
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
        logger.trace("Nombre de mains récupérés dans BDD : " + nombreMains);

        ConnexionBDD.fermerSession(session);
        logger.trace("Chemins récupérés dans BDD");
    }

    // va chercher tout seul les noms de dossiers
    public abstract boolean autoDetection();

    public WorkerAffichable importer() {
        // va importer tous les fichiers des dossiers qui existent

        // on construit d'abord la liste des fichiers à importer
        List<Path> nouveauxFichiers = listerNouveauxFichiers();
        if (nouveauxFichiers.isEmpty()) return null;

        WorkerAffichable worker =
                new WorkerImportation("Importer " + nomRoom, nouveauxFichiers.size(), nouveauxFichiers);


        logger.info("Worker créé pour import : " + nomRoom);

        return worker;
    }

    private List<Path> listerNouveauxFichiers() {
        List<Path> nouveauxFichiers = new ArrayList<>();

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
                        }
                    }
                }
            }
            catch (IOException e) {
                //log pas sensible
                //on continue le traitement
                logger.warn("Impossible de lire le fichier", e);
            }
        }

        return nouveauxFichiers;
    }

    public boolean ajouterDossier(Path cheminDuDossier) {
        boolean existant = false;
        Session session = ConnexionBDD.ouvrirSession();
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
                ConnexionBDD.fermerSession(session);
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
                ConnexionBDD.fermerSession(session);
                return false;
            }
        }
        transaction.commit();
        ConnexionBDD.fermerSession(session);
        return true;

    }

    public boolean supprimerDossier(String cheminDuDossier) {
        //on le désactive simplement

        for (DossierImport dossierCourant : dossierImports) {
            Path dossierExistant = dossierCourant.getChemin();
            if (cheminDuDossier.equals(dossierExistant.toString())) {
                logger.info("Dossier trouvé");
                Session session = ConnexionBDD.ouvrirSession();
                Transaction transaction = session.beginTransaction();
                session.merge(dossierCourant);
                dossierCourant.actif = false;
                transaction.commit();
                ConnexionBDD.fermerSession(session);
                return true;
            }
        }

        logger.warn("Dossier à supprimer non trouvé");
        return false;
    }

    protected abstract Integer ajouterFichier(Path cheminDuFichier);

    private void fichierAjoute(Path cheminDuFichier) {
        // rajoute le nom du fichier dans la BDD et dans notre liste
        // ne gère pas le nom des fichiers d'import
        String nomFichier = cheminDuFichier.getFileName().toString();

        Session session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();

        for (DossierImport dossier: dossierImports) {
            if (cheminDuFichier.toString().contains(dossier.getChemin().toString())) {
                dossier.fichierAjoute();
                session.merge(dossier);
            }
        }
        transaction.commit();
        ConnexionBDD.fermerSession(session);

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
            logger.warn("Impossible de parcourir le dossier", e);
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
            if (dossier.actif && dossier.getnFichiersImportes() > 0) return true;
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

    public class WorkerImportation extends WorkerAffichable {
        private final List<Path> nouveauxFichiers;

        public WorkerImportation(String nomTache, int nombreOperations, List<Path> nouveauxFichiers) {
            super(nomTache, nombreOperations);
            this.nouveauxFichiers = nouveauxFichiers;
        }

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
                    //todo : est ce qu'on enregistre quand même les fichiers bugués pour ne pas les retraiter??
                    else {
                        logger.warn("Fichier non ajouté");
                    }
                    publish(++i);
                } catch (Exception e) {
                    //log pas sensible
                    //on continue le traitement
                    logger.warn("Impossible d'ajouter le fichier", e);
                    gestionInterruption();
                    //on veut quand même ajouter le nombre de mains
                    break;
                    //todo : on pourrait capturer les exceptions ici, continuer le traitement sauf si trop d'erreurs
                    // todo affiche un message de succès ....
                }
            }
            Session session = ConnexionBDD.ouvrirSession();
            Transaction transaction = session.beginTransaction();
            DataRoom dataRoom = ObjetUnique.dataRoom(room);
            session.merge(dataRoom);
            dataRoom.addNombreMains(mainsAjouteesTotal);
            session.merge(dataRoom);
            transaction.commit();
            ConnexionBDD.fermerSession(session);

            return null;
        }
    }
}
