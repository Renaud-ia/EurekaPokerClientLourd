package analyzor;

import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.*;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.ComboReel;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;

import java.util.List;
import java.util.Objects;

public class TestCombo {
    public static void main(String[] args) {
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(3, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.CALL);
        noeudAbstrait.ajouterAction(Move.ALL_IN);
    }
}
