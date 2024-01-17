package analyzor;

import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.parties.*;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.ComboReel;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;

import java.util.List;
import java.util.Objects;

public class TestCombo {
    public static void main(String[] args) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<MainEnregistree> entreeCriteria = builder.createQuery(MainEnregistree.class);
        Root<MainEnregistree> mainRoot = entreeCriteria.from(MainEnregistree.class);

        List<MainEnregistree> listMains = session.createQuery(entreeCriteria).setMaxResults(10000).getResultList();

        //on ferme la session il faudra remerger les objets si on a besoin de les modifier
        ConnexionBDD.fermerSession(session);

        for (MainEnregistree mainEnregistree : listMains) {
            int comboIntHero = mainEnregistree.getComboHero();
            if (comboIntHero == 0) continue;
            ComboReel comboMain = new ComboReel(comboIntHero);
            ComboIso isoComboMain = new ComboIso(comboMain);
            System.out.println(isoComboMain);
        }
    }

}
