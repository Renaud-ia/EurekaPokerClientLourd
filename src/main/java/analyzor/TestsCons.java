package analyzor;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.utils.RequetesBDD;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestsCons {
    public static void main(String[] args) {
        RangeIso nouvelleRange = new RangeIso();

        NoeudAction noeudPreflop = new NoeudPreflop(184L, 25.4f, 2f, 3f);
        FormatSolution formatSolution = new FormatSolution(Variante.PokerFormat.SPIN, false, false, 3, 0, 50);

        RequetesBDD.ouvrirSession();
        Session session2 = RequetesBDD.getSession();
        Transaction transaction = session2.beginTransaction();
        session2.merge(formatSolution);
        transaction.commit();

        Transaction transaction2 = session2.beginTransaction();
        noeudPreflop.setFormatSolution(formatSolution);
        session2.persist(noeudPreflop);
        transaction2.commit();

        Transaction transaction3 = session2.beginTransaction();
        nouvelleRange.setNoeudAction(noeudPreflop);
        nouvelleRange.setProfil(new ProfilJoueur("villain"));
        ComboIso comboIso = new ComboIso("A5s");
        ComboIso combo2 = comboIso.copie();
        combo2.setValeur(0.5f);
        System.out.println(combo2.getValeur());
        session2.persist(combo2);
        nouvelleRange.ajouterCombo(combo2);
        session2.persist(nouvelleRange);
        transaction3.commit();
        RequetesBDD.fermerSession();
    }
}