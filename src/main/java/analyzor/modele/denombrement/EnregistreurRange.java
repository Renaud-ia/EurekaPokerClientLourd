package analyzor.modele.denombrement;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.bdd.ConnexionBDD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class EnregistreurRange {
    private static final Logger logger = LogManager.getLogger(EnregistreurRange.class);
    private final FormatSolution formatSolution;
    private final ProfilJoueur profilJoueur;
    public EnregistreurRange(FormatSolution formatSolution, ProfilJoueur profilJoueur) {
        this.formatSolution = formatSolution;
        this.profilJoueur = profilJoueur;
    }

    public void effacerRanges() {
        //todo
        Session session = ConnexionBDD.ouvrirSession();

        ConnexionBDD.fermerSession(session);
    }

    public boolean rangeExistante(Long idNoeudTheorique) {
        //todo
        return false;
    }

    public void sauvegarderRanges(List<ComboDenombrable> combosEquilibres,
                                         NoeudDenombrable noeudDenombrable) {
        // on crée une range par action
        // important on utilise les mêmes méthodes que pour dénombrement
        for (int i = 0; i < noeudDenombrable.getNoeudSansFold().size(); i++) {
            NoeudAction noeudAction = noeudDenombrable.getNoeudSansFold().get(i);
            creerRange(combosEquilibres, noeudAction, i);
        }

        // on fait la même chose pour le fold
        NoeudAction noeudFold = noeudDenombrable.getNoeudFold();
        if (noeudFold == null) {
            logger.debug("Pas de noeud fold");
        }
        else creerRange(combosEquilibres, noeudFold,
                noeudDenombrable.getNoeudSansFold().size());
    }

    private void creerRange(List<ComboDenombrable> combosEquilibres,
                                   NoeudAction noeudAction, int indexStrategie) {
        if (combosEquilibres.get(0) instanceof DenombrableIso) {
            creerRangeIso(combosEquilibres, noeudAction, indexStrategie);
        }
    }

    private void creerRangeIso(List<ComboDenombrable> combosEquilibres,
                                      NoeudAction noeudAction, int indexStrategie) {
        RangeIso nouvelleRange = new RangeIso();

        if (!(noeudAction instanceof NoeudPreflop)) throw new IllegalArgumentException("Pas un noeud préflop");

        NoeudPreflop noeudPreflop = (NoeudPreflop) noeudAction;

        Session session = ConnexionBDD.ouvrirSession();

        Transaction transactionRange = session.beginTransaction();
        session.merge(profilJoueur);
        session.merge(noeudPreflop);
        nouvelleRange.setProfil(profilJoueur);
        nouvelleRange.setNoeudAction(noeudPreflop);

        for (ComboDenombrable comboDenombrable : combosEquilibres) {
            if (!(comboDenombrable instanceof DenombrableIso))
                throw new IllegalArgumentException("Un combo n'est pas iso dans la range ISO");
            ComboIso comboIso = ((DenombrableIso)comboDenombrable).getCombo().copie();
            comboIso.setValeur(comboDenombrable.getStrategie()[indexStrategie]);
            session.persist(comboIso);
            nouvelleRange.ajouterCombo(comboIso);
        }


        session.persist(nouvelleRange);
        transactionRange.commit();

        ConnexionBDD.fermerSession(session);

        logger.debug("Range enregistree avec succes");
    }
}
