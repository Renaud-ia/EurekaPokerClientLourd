package analyzor.modele.simulation;

import analyzor.modele.extraction.TableImport;
import analyzor.modele.parties.Action;
import analyzor.modele.parties.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableImportTest {
    @Test
    void joueursAjoutesSontActifs() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);
        tableImport.ajouterJoueur("SB", 1, 10000, 3, null);
        tableImport.ajouterJoueur("BB", 2, 10000, 3, null);

        tableImport.nouveauTour();

        tableImport.setJoueur("BTN");

        assertEquals(3, tableImport.nombreJoueursActifs());
    }

    @Test
    void foldRetireUnJoueurActif() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);
        tableImport.ajouterJoueur("SB", 1, 10000, 3, null);
        tableImport.ajouterJoueur("BB", 2, 10000, 3, null);

        tableImport.nouveauTour();
        tableImport.ajouterAction("BTN", Move.FOLD, 0, false);

        tableImport.setJoueur("SB");

        assertEquals(2, tableImport.nombreJoueursActifs());
    }

    @Test
    void AllInRetireUnJoueurActif() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);
        tableImport.ajouterJoueur("SB", 1, 10000, 3, null);
        tableImport.ajouterJoueur("BB", 2, 10000, 3, null);

        tableImport.nouveauTour();
        tableImport.ajouterAction("BTN", Move.ALL_IN, 10000, false);

        tableImport.setJoueur("SB");

        assertEquals(2, tableImport.nombreJoueursActifs());
    }

    @Test
    void AnteIncrementeLePotTotal() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);
        tableImport.ajouterJoueur("SB", 1, 10000, 3, null);
        tableImport.ajouterJoueur("BB", 2, 10000, 3, null);

        tableImport.nouveauTour();
        tableImport.ajouterAnte("BTN", 10);

        assertEquals(10, tableImport.getPotTotal());

        tableImport.ajouterAnte("SB", 10);

        assertEquals(20, tableImport.getPotTotal());

        tableImport.ajouterAnte("BB", 10);

        assertEquals(30, tableImport.getPotTotal());
    }

    @Test
    void AnteSontRetireesDuStack() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);
        tableImport.ajouterJoueur("SB", 1, 10000, 3, null);
        tableImport.ajouterJoueur("BB", 2, 10000, 3, null);

        tableImport.nouveauTour();
        tableImport.ajouterAnte("BTN", 10);

        assertEquals(9990, tableImport.getStackJoueur("BTN"));
    }

    @Test
    void BlindesIncrementeLePotTotal() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("SB", 1, 10000, 3, null);
        tableImport.ajouterJoueur("BB", 2, 10000, 3, null);

        tableImport.nouveauTour();
        tableImport.ajouterBlindes("SB", 200);

        assertEquals(200, tableImport.getPotTotal());

        tableImport.ajouterBlindes("BB", 400);

        assertEquals(600, tableImport.getPotTotal());
    }

    @Test
    void BlindesSontRetireesDuStack() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);

        tableImport.nouveauTour();
        tableImport.ajouterBlindes("BTN", 200);

        assertEquals(9800, tableImport.getStackJoueur("BTN"));
    }

    @Test
    void raiseEstConvertiEnAllIn() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);

        tableImport.nouveauTour();
        Action actionCorrigee = tableImport.ajouterAction("BTN", Move.RAISE, 9900, true);

        assertEquals(Move.ALL_IN, actionCorrigee.getMove());
    }



    @Test
    void montantRaiseEstBienCorrige() {
        TableImport tableImport = new TableImport(1);
        tableImport.ajouterJoueur("BTN", 0, 10000, 3, null);
        tableImport.nouveauTour();

        tableImport.ajouterBlindes("BTN", 200);

        Action actionCorrigee = tableImport.ajouterAction("BTN", Move.RAISE, 1000, true);

        assertEquals(800, actionCorrigee.getBetSize());

        Action actionCorrigee2 = tableImport.ajouterAction("BTN", Move.RAISE, 2000, true);
        assertEquals(1000, actionCorrigee2.getBetSize());
    }

}
