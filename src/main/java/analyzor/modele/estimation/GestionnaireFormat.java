package analyzor.modele.estimation;

import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.ErreurBDD;
import analyzor.modele.parties.*;
import analyzor.modele.bdd.ConnexionBDD;
import jakarta.persistence.criteria.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GestionnaireFormat {
    private static Session session;

    

    
    public static FormatSolution ajouterFormat(FormatSolution formatSolution) {
        session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();
        try {
            actualiserNombreParties(formatSolution);
            session.persist(formatSolution);
            transaction.commit();
            ConnexionBDD.fermerSession(session);
            return formatSolution;
        }
        catch (Exception e) {
            transaction.rollback();
            ConnexionBDD.fermerSession(session);
            return null;
        }
    }

    
    public static void changerNomFormat(Long idBDD, String nouveauNom) {
        Session session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> criteria = builder.createQuery(FormatSolution.class);
        Root<FormatSolution> root = criteria.from(FormatSolution.class);

        criteria.where(builder.equal(root.get("id"), idBDD));

        FormatSolution entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            throw new ErreurBDD("Format solution non trouvé dans BBD");
        }
        entite.changerNom(nouveauNom);
        session.merge(entite);
        transaction.commit();

        ConnexionBDD.fermerSession(session);
    }

    
    public static void actualiserNombreParties(FormatSolution formatSolution) {
        boolean sessionOuverte;
        if (session == null || !session.isOpen()) {
            sessionOuverte = false;
            session = ConnexionBDD.ouvrirSession();
        }
        else {
            sessionOuverte = true;
        }

        List<Variante> variantes =
                selectionnerVariantes(formatSolution);

        List<Partie> parties =
                selectionnerParties(variantes, formatSolution);

        int nParties = parties.size();
        formatSolution.setNombreParties(nParties);

        if (!sessionOuverte) {
            Transaction transaction = session.beginTransaction();
            session.merge(formatSolution);
            transaction.commit();
            ConnexionBDD.fermerSession(session);
        }

    }

    public static List<FormatSolution> formatsDisponibles() {

        Session session = ConnexionBDD.ouvrirSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<FormatSolution> query = cb.createQuery(FormatSolution.class);
        Root<FormatSolution> formatSolutionRoot = query.from(FormatSolution.class);
        query.select(formatSolutionRoot);

        List<FormatSolution> formatsDispo = session.createQuery(query).getResultList();
        ConnexionBDD.fermerSession(session);

        return formatsDispo;
    }

    
    public static void supprimerFormat(long idBDD) {
        Session session = ConnexionBDD.ouvrirSession();

        Transaction tx = session.beginTransaction();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> criteria = builder.createQuery(FormatSolution.class);
        Root<FormatSolution> root = criteria.from(FormatSolution.class);

        criteria.where(builder.equal(root.get("id"), idBDD));

        FormatSolution formatSolution = session.createQuery(criteria).uniqueResult();

        if (formatSolution != null) {
            reinitialiserFormat(session, formatSolution);
            session.remove(formatSolution);
        }

        tx.commit();
        ConnexionBDD.fermerSession(session);
    }

    
    public static void reinitialiserFormat(FormatSolution formatSolution) {
        Session session = ConnexionBDD.ouvrirSession();

        Transaction tx = session.beginTransaction();

        if (formatSolution != null) {
            reinitialiserFormat(session, formatSolution);
            formatSolution.setNonCalcule();
        }

        tx.commit();
        ConnexionBDD.fermerSession(session);
    }

    private static void reinitialiserFormat(Session session, FormatSolution formatSolution) {
        
        CriteriaBuilder builderNoeuds = session.getCriteriaBuilder();
        CriteriaQuery<NoeudSituation> criteriaNoeuds = builderNoeuds.createQuery(NoeudSituation.class);
        Root<NoeudSituation> rootNoeuds = criteriaNoeuds.from(NoeudSituation.class);

        criteriaNoeuds.where(builderNoeuds.equal(rootNoeuds.get("formatSolution"), formatSolution));
        List<NoeudSituation> noeudSituations = session.createQuery(criteriaNoeuds).getResultList();
        for (NoeudSituation noeudSituation : noeudSituations) {
            session.remove(noeudSituation);
        }
    }

    public static FormatSolution getFormatSolution(Long idBDD) {
        Session session = ConnexionBDD.ouvrirSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> criteria = builder.createQuery(FormatSolution.class);
        Root<FormatSolution> root = criteria.from(FormatSolution.class);

        criteria.where(builder.equal(root.get("id"), idBDD));

        FormatSolution entite = session.createQuery(criteria).uniqueResult();

        if (entite == null) {
            throw new ErreurBDD("Format solution non trouvé dans BBD");
        }

        ConnexionBDD.fermerSession(session);

        return entite;
    }

    
    

    
    public static List<Entree> getEntrees(FormatSolution formatSolution,
                                          List<NoeudAbstrait> situationsGroupees,
                                          ProfilJoueur profilJoueur) {
        session = ConnexionBDD.ouvrirSession();
        List<Long> idNoeudCherches = getIdNoeuds(situationsGroupees);

        List<Variante> variantes =
                selectionnerVariantes(formatSolution);

        List<Partie> parties =
                selectionnerParties(variantes, formatSolution);

        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Entree> entreeCriteria = builder.createQuery(Entree.class);
        Root<Entree> entreeRoot = entreeCriteria.from(Entree.class);

        
        Join<Entree, Joueur> joueurJoin = entreeRoot.join("joueur");
        entreeRoot.fetch("joueur", JoinType.INNER);
        Join<Entree, TourMain> tourMainJoin = entreeRoot.join("tourMain");
        Join<TourMain, MainEnregistree> mainJoin = tourMainJoin.join("main");

        if (Objects.equals(profilJoueur.getNom(), ProfilJoueur.nomProfilHero)) {
            entreeRoot.fetch("tourMain", JoinType.INNER);
            
        }

        Predicate isMemberPredicate = builder.isMember(profilJoueur, joueurJoin.get("profils"));

        entreeCriteria.select(entreeRoot).where(
                builder.isTrue(entreeRoot.get("idNoeudTheorique").in(idNoeudCherches)),
                builder.isTrue(mainJoin.get("partie").in(parties)),
                isMemberPredicate
        );

        List<Entree> listEntrees = session.createQuery(entreeCriteria).getResultList();

        
        ConnexionBDD.fermerSession(session);

        return listEntrees;
    }

    public static void setNombreSituations(FormatSolution formatSolution, int size) {
        session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();
        formatSolution.setNombreSituations(size);
        formatSolution.setNombrePartiesCalculees(formatSolution.getNombreParties());
        session.merge(formatSolution);
        transaction.commit();
        ConnexionBDD.fermerSession(session);
    }

    public static void situationResolue(FormatSolution formatSolution, int nombreSituationsResolues, float pctAvancement) {
        session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();
        formatSolution.setNombreSituationsResolues(nombreSituationsResolues, pctAvancement);
        session.merge(formatSolution);
        transaction.commit();
        ConnexionBDD.fermerSession(session);
    }

    public static void roundResolu(FormatSolution formatSolution, TourMain.Round round) {
        session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();
        formatSolution.setCalcule(round);
        session.merge(formatSolution);
        transaction.commit();
        ConnexionBDD.fermerSession(session);
    }


    

    private static List<Variante> selectionnerVariantes(FormatSolution formatSolution) {
        
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Variante> varianteCriteria = builder.createQuery(Variante.class);
        Root<Variante> varianteRoot = varianteCriteria.from(Variante.class);

        varianteCriteria.select(varianteRoot).where(
                builder.equal(varianteRoot.get("format"), formatSolution.getPokerFormat()),
                builder.greaterThanOrEqualTo(varianteRoot.get("ante"), formatSolution.getAnteMin()),
                builder.lessThanOrEqualTo(varianteRoot.get("ante"), formatSolution.getAnteMax()),
                builder.equal(varianteRoot.get("ko"), formatSolution.getKO()),
                builder.equal(varianteRoot.get("nombreJoueurs"), formatSolution.getNombreJoueurs()),
                builder.greaterThanOrEqualTo(varianteRoot.get("buyIn"), formatSolution.getMinBuyIn()),
                builder.lessThanOrEqualTo(varianteRoot.get("buyIn"), formatSolution.getMaxBuyIn()),
                builder.greaterThanOrEqualTo(varianteRoot.get("rake"), formatSolution.getRakeMin()),
                builder.lessThanOrEqualTo(varianteRoot.get("rake"), formatSolution.getRakeMax())
        );

        return session.createQuery(varianteCriteria).getResultList();
    }

    private static List<Partie> selectionnerParties(List<Variante> variantes, FormatSolution formatSolution) {
        
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Partie> partieCriteria = builder.createQuery(Partie.class);
        Root<Partie> partieRoot = partieCriteria.from(Partie.class);

        partieCriteria.select(partieRoot).where(
                builder.isTrue(partieRoot.get("variante").in(variantes)),
                builder.greaterThanOrEqualTo(partieRoot.get("dPlayed"), formatSolution.getJoueApres()),
                builder.lessThanOrEqualTo(partieRoot.get("dPlayed"), formatSolution.getJoueAvant())
        );

        return session.createQuery(partieCriteria).getResultList();
    }

    private static List<Long> getIdNoeuds(List<NoeudAbstrait> situationsGroupees) {
        List<Long> idNoeuds = new ArrayList<>();
        for (NoeudAbstrait noeudCherche : situationsGroupees) {
            idNoeuds.add(noeudCherche.toLong());
        }

        return idNoeuds;
    }
}
