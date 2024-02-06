package analyzor;

import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.poker.ComboIso;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

public class ImportDebug {
    public static void main(String[] args) {
        NoeudAbstrait noeudAbstrait = new NoeudAbstrait(3, TourMain.Round.PREFLOP);
        noeudAbstrait.ajouterAction(Move.RAISE);

        Session session = ConnexionBDD.ouvrirSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Entree> entreeCriteria = builder.createQuery(Entree.class);
        Root<Entree> entreeRoot = entreeCriteria.from(Entree.class);

        entreeCriteria.select(entreeRoot).where(
                builder.equal(entreeRoot.get("idNoeudTheorique"), noeudAbstrait.toLong())
        );

        System.out.println("Nombre entrées : " + session.createQuery(entreeCriteria).getResultList().size());

        ConnexionBDD.fermerSession(session);
    }
}
