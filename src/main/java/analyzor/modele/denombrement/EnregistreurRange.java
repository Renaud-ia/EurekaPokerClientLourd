package analyzor.modele.denombrement;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.RangeIso;
import analyzor.modele.utils.RequetesBDD;
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
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        RequetesBDD.fermerSession();
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
            logger.error("Pas de noeud fold");
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

        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        Transaction transactionNoeud = session.beginTransaction();
        noeudAction.setFormatSolution(formatSolution);
        session.persist(noeudPreflop);
        transactionNoeud.commit();

        // il faut faire deux transactions séparées car sinon ça putain de bug!!!
        Transaction transactionRange = session.beginTransaction();
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

        RequetesBDD.fermerSession();

        logger.debug("Range enregistree avec succes");
    }
}
