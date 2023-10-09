package analyzor.modele.extraction;

import analyzor.modele.exceptions.ErreurInterne;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {
    public static void main(String[] args) {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        LocalDateTime Date = LocalDateTime.now();
        Transaction transaction = session.beginTransaction();

        Variante variante = new Variante(PokerRoom.WINAMAX, Variante.PokerFormat.MTT, Variante.Vitesse.SEMI_TURBO, 0.525f, false);
        Partie partie = new Partie(variante, 152250, 10055, "PT588", "TES2T85", Date);
        variante.getParties().add(partie);
        MainEnregistree main = new MainEnregistree(136, 52, partie);
        TourMain tourMain = new TourMain(TourMain.Round.PREFLOP, main, new Board(), 8);
        main.getTours().add(tourMain);

        session.merge(tourMain);
        session.merge(main);


        session.merge(partie);
        //variante.genererId();
        partie.setBuyIn(105);
        session.merge(partie);

        session.merge(variante);

        transaction.commit();
        System.out.println(variante.getParties());
        RequetesBDD.fermerSession();
        }
}
