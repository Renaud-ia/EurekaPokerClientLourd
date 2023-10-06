package analyzor.modele.extraction;

import analyzor.modele.logging.GestionnaireLog;

import java.nio.file.Path;
import java.util.logging.Logger;

public class LecteurWinamax implements LecteurPartie {
    private Logger logger = GestionnaireLog.getLogger("LecteurWinamax");
    private final Path cheminDuFichier;
    private final String nomFichier;
    public LecteurWinamax(Path cheminDuFichier) {
        this.cheminDuFichier = cheminDuFichier;
        nomFichier = cheminDuFichier.getFileName().toString();
    }
    @Override
    public Integer sauvegarderPartie() {
        logger.fine("Enregistrement de la partie dans la BDD");
        return null;
    }

    @Override
    public boolean fichierEstValide() {
        // on prend les summary comme valides
        boolean correspond = nomFichier.matches("^[0-9]{8}_.+real_holdem_no-limit(_summary)?\\.txt$");

        if (correspond) {
            logger.fine("Format nom de fichier reconnu");
            return true;
        } else {
            logger.fine("Fichier non valide : " + nomFichier);
            return false;
        }

    }

}
