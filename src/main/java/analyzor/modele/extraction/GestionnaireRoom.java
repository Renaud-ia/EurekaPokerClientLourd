package analyzor.modele.extraction;

import analyzor.modele.parties.PokerRoom;
import analyzor.modele.bdd.ConnexionBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class GestionnaireRoom implements ControleGestionnaire {
    
    protected String nomRoom;
    protected ImageIcon icone;
    protected List<FichierImport> fichiersImportes;
    private List<DossierImport> dossierImports;
    protected int nombreMains;
    private final PokerRoom room;
    protected final List<String> dossiersDetection = new ArrayList<>();

    
    protected GestionnaireRoom(PokerRoom room) {
        this.room = room;
        this.nomRoom = room.toString();
        actualiserDonnees();
        ajouterDossiersRecherche();
    }

    

    
    public void actualiserDonnees() {
        
        Session session = ConnexionBDD.ouvrirSession();
        CriteriaBuilder cbDossiers = session.getCriteriaBuilder();
        CriteriaQuery<DossierImport> queryDossiers = cbDossiers.createQuery(DossierImport.class);
        Root<DossierImport> rootDossier = queryDossiers.from(DossierImport.class);
        queryDossiers.where(cbDossiers.equal(rootDossier.get("room"), this.room));
        dossierImports = session.createQuery(queryDossiers).getResultList();

        CriteriaBuilder cbFichiers = session.getCriteriaBuilder();
        CriteriaQuery<FichierImport> queryFichiers = cbFichiers.createQuery(FichierImport.class);
        Root<FichierImport> rootFichier = queryFichiers.from(FichierImport.class);
        queryFichiers.where(cbFichiers.equal(rootFichier.get("room"), this.room));
        fichiersImportes = session.createQuery(queryFichiers).getResultList();

        ConnexionBDD.fermerSession(session);
    }

    
    public boolean autoDetection() {
        boolean dossiersAjoutes = false;
        for (String chemin : dossiersDetection) {
            if (ajouterDossier(chemin)) {
                dossiersAjoutes = true;
            }
        }

        return dossiersAjoutes;
    }

    public abstract List<LecteurPartie> importer();

    public boolean ajouterDossier(String nomChemin) {
        Path cheminDuDossier;
        try {
            cheminDuDossier = Paths.get(nomChemin);
        }
        catch (Exception e) {
            return false;
        }

        if(!(Files.exists(cheminDuDossier))) return false;

        boolean existant = false;
        Session session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();

        for (DossierImport dossierCourant : dossierImports) {
            Path dossierExistant = dossierCourant.getChemin();
            if (cheminDuDossier.toString().equals(dossierExistant.toString())) {
                dossierCourant.actif = true;
                existant = true;
                session.merge(dossierCourant);
                break;
            }

            else if (cheminDuDossier.startsWith(dossierExistant)) {
                transaction.rollback();
                ConnexionBDD.fermerSession(session);
                return false;
            }
        }

        if (existant) {
            transaction.commit();
            ConnexionBDD.fermerSession(session);
            return false;
        }

        if (dossierEstValide(cheminDuDossier)) {
            DossierImport dossierStocke = new DossierImport(this.room, cheminDuDossier);
            dossierStocke.actif = true;
            this.dossierImports.add(dossierStocke);
            session.merge(dossierStocke);
        }
        else {
            transaction.rollback();
            ConnexionBDD.fermerSession(session);
            return false;
        }

        transaction.commit();
        ConnexionBDD.fermerSession(session);

        return true;


    }

    public boolean supprimerDossier(String cheminDuDossier) {
        
        for (DossierImport dossierCourant : dossierImports) {
            Path dossierExistant = dossierCourant.getChemin();
            if (cheminDuDossier.equals(dossierExistant.toString())) {
                Session session = ConnexionBDD.ouvrirSession();
                Transaction transaction = session.beginTransaction();
                dossierCourant.desactiver();
                session.merge(dossierCourant);
                transaction.commit();
                ConnexionBDD.fermerSession(session);

                return true;
            }
        }
        return false;
    }

    

    public String getNomRoom(){
        return nomRoom;
    }
    public boolean getConfiguration() {
        for (DossierImport dossier : dossierImports) {
            if (dossier.actif) return true;
        }
        return false;
    }

    public List<String> getDossiers() {
        List<String> nomsDossiers = new ArrayList<>();
        for (DossierImport dossierImport : dossierImports) {
            if (dossierImport.estActif()) {
                nomsDossiers.add(dossierImport.getChemin().toString());
            }
        }

        return nomsDossiers;
    }

    public int getNombreFichiersImportes() {
        int nFichiersImportes = 0;
        for (FichierImport fichierImport : fichiersImportes) {
            if (fichierImport.estReussi()) {
                nFichiersImportes++;
            }
        }

        return nFichiersImportes;
    }

    public int getNombreMainsImportees() {
        int nMainsImportees = 0;
        for (FichierImport fichierImport : fichiersImportes) {
            nMainsImportees += fichierImport.getNombreMains();
        }

        return nMainsImportees;
    }

    public int getNombreErreursImport() {
        int nErreurs = 0;
        for (FichierImport fichierImport : fichiersImportes) {
            if (!fichierImport.estReussi()) {
                nErreurs++;
            }
        }

        return nErreurs;
    }

    public List<FichierImport> getPartiesNonImportees() {
        List<FichierImport> fichiersNonImportees = new ArrayList<>();
        for (FichierImport fichierImport : fichiersImportes) {
            if (!fichierImport.estReussi()) {
                fichiersNonImportees.add(fichierImport);
            }
        }

        return fichiersNonImportees;
    }

    public void supprimerImportsRates() {
        Session session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();

        List<FichierImport> fichiersNonImportees = new ArrayList<>();
        for (FichierImport fichierImport : fichiersImportes) {
            if (!fichierImport.estReussi()) {
                fichiersNonImportees.add(fichierImport);
                session.remove(fichierImport);
            }
        }
        transaction.commit();
        ConnexionBDD.fermerSession(session);

        fichiersImportes.removeAll(fichiersNonImportees);
        fichiersNonImportees.clear();
    }

    

    protected List<Path> listerNouveauxFichiers() {
        List<Path> nouveauxFichiers = new ArrayList<>();

        for (DossierImport dossierCourant : dossierImports) {
            Path dossierExistant = dossierCourant.getChemin();
            try (Stream<Path> stream = Files.walk(dossierExistant)) {
                Iterator<Path> iterator = stream.iterator();
                while (iterator.hasNext()) {
                    Path currentPath = iterator.next();
                    if (Files.isRegularFile(currentPath)) {
                        String nomFichier = currentPath.getFileName().toString();
                        if (fichierEstValide(currentPath)) {
                            boolean dejaAjoute = false;
                            for (FichierImport fichierImport : fichiersImportes) {
                                if (nomFichier.equals(fichierImport.getNomFichier())) {
                                    dejaAjoute = true;
                                    break;
                                }
                            }
                            if (!dejaAjoute) {
                                nouveauxFichiers.add(currentPath);
                            }
                        }
                    }
                }
            }
            catch (IOException ignored) {
            }
        }

        return nouveauxFichiers;
    }

    protected abstract boolean fichierEstValide(Path cheminDuFichier);

    protected abstract void ajouterDossiersRecherche();

    

    private boolean dossierEstValide(Path cheminDuDossier) {
        final int MAX_DEPTH = 4;
        final int FICHIERS_TESTES = 10;

        boolean auMoinsUnFichierEstValide;

        try (Stream<Path> stream = Files.walk(cheminDuDossier, MAX_DEPTH)) {
            auMoinsUnFichierEstValide = stream.filter(Files::isRegularFile)
                    .limit(FICHIERS_TESTES)
                    .anyMatch(this::fichierEstValide);
        }

        catch (IOException e) {
            return false;
        }

        return auMoinsUnFichierEstValide;
    }

    @Override
    public ImageIcon getIcone() {
        return icone;
    }

    
    protected List<String> trouverDossiersHistoriquesParUser(String nomDossier, String nomSousDossier) {
        File dossier = new File(nomDossier);
        List<String> dossiersAvecHistorique = new ArrayList<>();

        
        if (dossier.exists() && dossier.isDirectory()) {
            File[] sousDossiers = dossier.listFiles();

            if (sousDossiers == null) return dossiersAvecHistorique;

            for (File sousDossier : sousDossiers) {
                if (sousDossier.isDirectory()) {
                    
                    File dossierPotentielHistorique = new File(sousDossier, nomSousDossier);
                    if (dossierPotentielHistorique.exists() && dossierPotentielHistorique.isDirectory()) {
                        dossiersAvecHistorique.add(sousDossier.getAbsolutePath());
                    }
                }
            }
        }

        return dossiersAvecHistorique;
    }


}
