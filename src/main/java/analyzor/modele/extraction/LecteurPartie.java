package analyzor.modele.extraction;

import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.extraction.exceptions.FichierManquant;
import analyzor.modele.extraction.exceptions.FormatNonPrisEnCharge;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.Variante;
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
        this.pokerRoom = pokerRoom;
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
                logger.info("Transaction commitée avec succès");
            }

            else {
                transaction.rollback();
                logger.warn("Problème de lecture du fichier : " + cheminDuFichier, erreurImportation);
                nombreDeMainsImportees = 0;

                statutImport = switch (erreurImportation) {
                    case FichierManquant fichierManquant -> FichierImport.StatutImport.FICHIER_MANQUANT;
                    case InformationsIncorrectes informationsIncorrectes ->
                            FichierImport.StatutImport.INFORMATIONS_INCORRECTES;
                    case IOException ioException -> FichierImport.StatutImport.FICHIER_CORROMPU;
                    case HibernateException hibernateException -> FichierImport.StatutImport.PROBLEME_BDD;
                    case FormatNonPrisEnCharge formatNonPrisEnCharge -> FichierImport.StatutImport.NON_PRIS_EN_CHARGE;
                    default -> FichierImport.StatutImport.AUTRE;
                };
            }
        }
        catch (Exception e) {
            logger.error("Pas réussi à commit la transaction");
            statutImport = FichierImport.StatutImport.PROBLEME_BDD;
        }

        Transaction transactionFichier = session.beginTransaction();
        FichierImport fichierImport = ObjetUnique.fichierImport(pokerRoom, nomFichier);
        fichierImport.setCheminComplet(cheminDuFichier.toString());
        fichierImport.setNombreMainsImportees(nombreDeMainsImportees);
        fichierImport.setStatut(statutImport);
        session.merge(fichierImport);
        transactionFichier.commit();

        ConnexionBDD.fermerSession(session);

        return nombreDeMainsImportees;
    }
}
