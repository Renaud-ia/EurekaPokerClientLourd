package analyzor;

import analyzor.modele.arbre.RecuperateurRange;
import analyzor.modele.bdd.ConnexionBDD;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestsPartieH2 {
    public static void main(String[] args) {
        Session session = ConnexionBDD.ouvrirSession();
        Session session2 = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();
        Transaction transaction1 = session.beginTransaction();
    }
}
