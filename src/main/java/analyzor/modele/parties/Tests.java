package analyzor.modele.parties;

import analyzor.modele.exceptions.ErreurInterne;
import analyzor.modele.logging.GestionnaireLog;

import java.util.logging.Logger;

public class Tests {
    public static void main(String[] args) {
        try {
            Situation situation = new Situation(0, 3, 1, 22);
            Situation situationTrouvee = (Situation) RequetesBDD.getOrCreate(situation, true);

            Logger logger = GestionnaireLog.getLogger("test");
            logger.info("Deuxième situation");

            Situation situation2 = new Situation();
            situation2.setRang(0);
            Situation situationTrouvee2 = (Situation) RequetesBDD.getOrCreate(situation2, true);
            System.out.println(situationTrouvee2 == null);
        }
        catch (ErreurInterne e) {
            System.exit(-1);
        }
    }
}
