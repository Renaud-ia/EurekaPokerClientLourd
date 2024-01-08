package analyzor.modele.bdd;

import analyzor.modele.extraction.FichierImport;
import analyzor.modele.parties.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * classe utilitaire qui garantit l'unicité des objets dans la base
 */
public class ObjetUnique {
    public static Joueur joueur(String nom) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Joueur> criteria = builder.createQuery(Joueur.class);
        Root<Joueur> root = criteria.from(Joueur.class);

        criteria.where(builder.equal(root.get("nom"), nom));

        Joueur entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            Transaction transaction = session.beginTransaction();
            entite = new Joueur(nom);
            session.persist(entite);
            transaction.commit();
        }

        ConnexionBDD.fermerSession(session);

        return entite;
    }

    public static ProfilJoueur profilJoueur(String nomProfil, boolean hero) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ProfilJoueur> criteria = builder.createQuery(ProfilJoueur.class);
        Root<ProfilJoueur> root = criteria.from(ProfilJoueur.class);

        if (nomProfil == null) {
            criteria.where(
                    builder.isNull(root.get("nomProfil")),
                    builder.equal(root.get("hero"), hero)
            );
        }
        else {
            criteria.where(
                    builder.equal(root.get("nomProfil"), nomProfil),
                    builder.equal(root.get("hero"), hero)
            );
        }


        ProfilJoueur entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            System.out.println("aucun résultat trouvé");
            Transaction transaction = session.beginTransaction();
            entite = new ProfilJoueur(nomProfil, hero);
            session.persist(entite);
            transaction.commit();
        }

        ConnexionBDD.fermerSession(session);

        return entite;
    }

    public static Variante variante(PokerRoom room, Variante.PokerFormat pokerFormat,
                                    Variante.Vitesse vitesse, float ante, boolean ko) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Variante> criteria = builder.createQuery(Variante.class);
        Root<Variante> root = criteria.from(Variante.class);

        criteria.where(
                builder.equal(root.get("room"), room),
                builder.equal(root.get("format"), pokerFormat),
                builder.equal(root.get("vitesse"), vitesse),
                builder.equal(root.get("ante"), ante),
                builder.equal(root.get("ko"), ko)
        );

        Variante entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            Transaction transaction = session.beginTransaction();
            entite = new Variante(room, pokerFormat,
                    vitesse, ante, ko);
            session.persist(entite);
            transaction.commit();
        }

        ConnexionBDD.fermerSession(session);

        return entite;

    }

    public static DataRoom dataRoom(PokerRoom room) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DataRoom> criteria = builder.createQuery(DataRoom.class);
        Root<DataRoom> root = criteria.from(DataRoom.class);

        criteria.where(
                builder.equal(root.get("room"), room)
        );

        DataRoom entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            Transaction transaction = session.beginTransaction();
            entite = new DataRoom(room);
            session.persist(entite);
            transaction.commit();
        }

        ConnexionBDD.fermerSession(session);

        return entite;
    }

    public static FichierImport fichierImport(PokerRoom room, String nomFichier) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FichierImport> criteria = builder.createQuery(FichierImport.class);
        Root<FichierImport> root = criteria.from(FichierImport.class);

        criteria.where(
                builder.equal(root.get("room"), room),
                builder.equal(root.get("nomFichier"), nomFichier)
        );

        FichierImport entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            Transaction transaction = session.beginTransaction();
            entite = new FichierImport(room, nomFichier);
            session.persist(entite);
            transaction.commit();
        }

        ConnexionBDD.fermerSession(session);

        return entite;

    }

    public static void main(String[] args) {
        Session session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();

        ProfilJoueur profilJoueur = ObjetUnique.profilJoueur(null, true);
        Joueur joueur = ObjetUnique.joueur("baba");

        joueur.setProfil(profilJoueur);
        profilJoueur.getJoueurs().add(joueur);
        session.merge(joueur);

        profilJoueur.changerNom("rherrh");
        session.merge(profilJoueur);

        transaction.commit();
        ConnexionBDD.fermerSession(session);
    }
}
