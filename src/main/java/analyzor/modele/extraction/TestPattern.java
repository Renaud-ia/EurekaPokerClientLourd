package analyzor.modele.extraction;

import analyzor.modele.exceptions.ErreurInterne;
import analyzor.modele.parties.*;
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

        Variante variante = new Variante(PokerRoom.WINAMAX, Variante.PokerFormat.MTT, Variante.Vitesse.TURBO, 12.5f, true);
        Partie partie = new Partie(variante, 152250, 5, "CesTMOI", "TEST285", Date);
        variante.getParties().add(partie);

        Transaction transaction = session.beginTransaction();
        variante.genererId();
        session.merge(variante);

        transaction.commit();
        System.out.println(variante.getParties());
        RequetesBDD.fermerSession();
        }
}
