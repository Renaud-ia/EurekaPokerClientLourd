package analyzor.modele.extraction;

import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.extraction.exceptions.ErreurImportation;
import analyzor.modele.extraction.exceptions.FichierManquant;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.extraction.winamax.LecteurWinamax;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.nio.file.Path;

public abstract class LecteurPartie {
    protected final Logger logger = LogManager.getLogger(LecteurPartie.class);
    protected final Path cheminDuFichier;
    protected final String nomFichier;
    protected Variante variante;
    protected PokerRoom pokerRoom;
    protected Session session;
    protected Transaction transaction;

    protected LecteurPartie(Path cheminDuFichier, PokerRoom pokerRoom) {
        this.cheminDuFichier = cheminDuFichier;
        this.nomFichier = cheminDuFichier.getFileName().toString();
    }

    protected void ouvrirTransaction() {
        session = ConnexionBDD.ouvrirSession();
        transaction = session.beginTransaction();
    }

    // retourne le nombre de mains enregistrées sinon null
    public abstract Integer sauvegarderPartie();

    public abstract boolean fichierEstValide();

    // on enregistre le fichier comme importé
    // on gère ici toutes les exceptions rencontrées
    protected Integer importTermine(Exception erreurImportation, Integer nombreDeMains) {
        FichierImport.StatutImport statutImport;
        Integer nombreDeMainsImportees = null;

        try {
            if (erreurImportation == null) {
                transaction.commit();
                statutImport = FichierImport.StatutImport.REUSSI;
                nombreDeMainsImportees = nombreDeMains;
            }

            else {
                transaction.rollback();
                logger.error("Problème de lecture du fichier : " + cheminDuFichier, erreurImportation);
                if (erreurImportation instanceof FichierManquant) {
                    statutImport = FichierImport.StatutImport.FICHIER_MANQUANT;
                } else if (erreurImportation instanceof InformationsIncorrectes) {
                    statutImport = FichierImport.StatutImport.INFORMATIONS_INCORRECTES;
                } else if (erreurImportation instanceof IOException) {
                    statutImport = FichierImport.StatutImport.FICHIER_CORROMPU;
                } else if (erreurImportation instanceof HibernateException) {
                    statutImport = FichierImport.StatutImport.PROBLEME_BDD;
                } else {
                    statutImport = FichierImport.StatutImport.AUTRE;
                }
            }
        }
        catch (Exception e) {
            logger.error("Pas réussi à commit la transaction");
            statutImport = FichierImport.StatutImport.PROBLEME_BDD;
        }

        Transaction transactionFichier = session.beginTransaction();
        FichierImport fichierImport = ObjetUnique.fichierImport(pokerRoom, nomFichier);
        fichierImport.setStatut(statutImport);
        session.merge(fichierImport);
        transactionFichier.commit();

        ConnexionBDD.fermerSession(session);

        return nombreDeMainsImportees;
    }
}
