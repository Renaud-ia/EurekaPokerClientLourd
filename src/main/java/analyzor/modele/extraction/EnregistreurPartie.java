package analyzor.modele.extraction;

import analyzor.modele.logging.GestionnaireLog;
import analyzor.modele.parties.Partie;
import analyzor.modele.parties.Variante;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class EnregistreurPartie {
    private static final Logger logger = GestionnaireLog.getLogger("EnregistreurPartie");

    private final int idMain;
    private final int montantBB;
    private final Partie partie;
    private final String nomHero;
    private final Variante.PokerRoom room;

    public EnregistreurPartie(FileHandler fileHandlerGestionnaire,
                              int idMain,
                              int montantBB,
                              Partie partie,
                              String nomHero,
                              Variante.PokerRoom room) {
        // configuration des logs
        GestionnaireLog.setHandler(logger, fileHandlerGestionnaire);
        GestionnaireLog.setHandler(logger, GestionnaireLog.warningImport);

        //initialisation
        this.idMain = idMain;
        this.montantBB = montantBB;
        this.partie = partie;
        this.nomHero = nomHero;
        this.room = room;
    }

}
