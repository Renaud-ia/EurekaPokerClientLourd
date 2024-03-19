package analyzor;

import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.parties.*;
import analyzor.modele.simulation.BuilderStackEffectif;
import analyzor.modele.simulation.StacksEffectifs;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;

import java.util.List;
import java.util.Objects;

public class DebugStackEffectif {
    public static void main(String[] args) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();

        // Création de l'objet CriteriaQuery
        CriteriaQuery<Entree> entreeCriteria = builder.createQuery(Entree.class);
        Root<Entree> entreeRoot = entreeCriteria.from(Entree.class);

        // Préparation de la requête basée sur les critères
        entreeCriteria.select(entreeRoot);

        // Exécution de la requête avec une limite de résultats
        List<Entree> listEntrees = session.createQuery(entreeCriteria).getResultList();

        for (Entree entree : listEntrees) {
            StacksEffectifs stacksEffectifs = BuilderStackEffectif.getStacksEffectifs(entree.getCodeStackEffectif());
            if (stacksEffectifs.getDonnees()[0] < stacksEffectifs.getDonnees()[1] && stacksEffectifs.getDonnees()[0] > 1) {
                System.out.println("DEUXIEME STACK EST SUPERIEUR : " + entree.getId());
            }
        }

        // Fermeture de la session
        ConnexionBDD.fermerSession(session);
    }
}
