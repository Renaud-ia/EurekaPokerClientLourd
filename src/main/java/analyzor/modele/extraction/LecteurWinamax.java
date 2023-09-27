package analyzor.modele.extraction;

import java.nio.file.Path;

public class LecteurWinamax implements LecteurPartie {
    public LecteurWinamax(Path cheminDuFichier) {
    }
    @Override
    public Integer sauvegarderPartie() {
        return null;
    }

    @Override
    public boolean fichierEstValide() {
        return false;
    }

}
