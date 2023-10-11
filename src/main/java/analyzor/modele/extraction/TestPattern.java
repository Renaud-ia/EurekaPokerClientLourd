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
        String ligne = "Seat 2: RendsL4rgent (button) showed [7s Ah] and won 9064 with One pair : Aces";
        Pattern patternNomGain = Pattern.compile(
                "Seat\\s\\d:\\s(?<playName>(?:(?!showed|won|[\\(\\)]).)*)\\s.+");
        Matcher matcher = patternNomGain.matcher(ligne);
        System.out.println(matcher.find());
        System.out.println(matcher.group("playName"));
        }
}
