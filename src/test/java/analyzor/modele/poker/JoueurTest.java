package analyzor.modele.poker;

import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Joueur;
import analyzor.modele.bdd.ConnexionBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class JoueurTest {
    @Test
    void egalite() {
        List<Joueur> joueurs = new ArrayList<>();


        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Entree> query = cb.createQuery(Entree.class);
        Root<Entree> entreeRoot = query.from(Entree.class);

        query.select(entreeRoot);

        int count = 0;
        for (Entree entree : session.createQuery(query).setMaxResults(100).getResultList()) {
            if (count++ > 100) break;
            Joueur joueur = entree.getJoueur();
            if (!(joueurs.contains(joueur))) {
                joueurs.add(joueur);
                System.out.println("Joueur ajoute : " + joueur);
            }
        }
    }
}
