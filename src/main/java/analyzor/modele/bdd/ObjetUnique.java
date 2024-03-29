package analyzor.modele.bdd;

import analyzor.modele.extraction.FichierImport;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import static analyzor.modele.parties.ProfilJoueur.nomProfilHero;
import static analyzor.modele.parties.ProfilJoueur.nomProfilVillain;


public class ObjetUnique {

    public static Joueur joueur(String nom, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Joueur> criteria = builder.createQuery(Joueur.class);
        Root<Joueur> root = criteria.from(Joueur.class);

        criteria.where(builder.equal(root.get("nom"), nom));

        Joueur entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            entite = new Joueur(nom);
            session.persist(entite);
        }

        return entite;
    }

    public static ProfilJoueur selectionnerHero() {
        return profilJoueur(nomProfilHero);
    }

    public static ProfilJoueur selectionnerVillain() {
        return profilJoueur(nomProfilVillain);
    }

    
    private static ProfilJoueur profilJoueur(String nomProfil) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ProfilJoueur> criteria = builder.createQuery(ProfilJoueur.class);
        Root<ProfilJoueur> root = criteria.from(ProfilJoueur.class);

        criteria.where(
                builder.equal(root.get("nomProfil"), nomProfil)
        );

        ProfilJoueur entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            Transaction transaction = session.beginTransaction();
            entite = new ProfilJoueur(nomProfil);
            session.persist(entite);
            transaction.commit();
        }

        ConnexionBDD.fermerSession(session);

        return entite;
    }

    public static Variante variante(Variante.VariantePoker variantePoker,
                                    Variante.PokerFormat pokerFormat,
                                    float buyIn,
                                    int nombreJoueurs,
                                    float ante,
                                    float rake,
                                    boolean ko) throws InformationsIncorrectes {


        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Variante> criteria = builder.createQuery(Variante.class);
        Root<Variante> root = criteria.from(Variante.class);

        criteria.where(
                builder.equal(root.get("variantePoker"), variantePoker),
                builder.equal(root.get("format"), pokerFormat),
                builder.equal(root.get("buyIn"), buyIn),
                builder.equal(root.get("nombreJoueurs"), nombreJoueurs),
                builder.equal(root.get("ante"), ante),
                builder.equal(root.get("rake"), rake),
                builder.equal(root.get("ko"), ko)
        );

        Variante entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            Transaction transaction = session.beginTransaction();
            entite = new Variante(variantePoker, pokerFormat, buyIn, nombreJoueurs, ante, rake, ko);
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
}
