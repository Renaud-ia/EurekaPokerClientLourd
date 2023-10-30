package analyzor.modele.denombrement;

import analyzor.modele.arbre.Classificateur;
import analyzor.modele.arbre.ClassificateurCumulatif;
import analyzor.modele.arbre.ClassificateurDynamique;
import analyzor.modele.arbre.ClassificateurSubset;

public class CompteurFactory {
    public static CompteurRange creeCompteur(Classificateur classificateur) {
        if (classificateur instanceof ClassificateurCumulatif) {
            return new CompteurIso();
        }
        else if (classificateur instanceof ClassificateurSubset || classificateur instanceof ClassificateurDynamique) {
            return new CompteurDynamique();
        }

        else {
            throw new IllegalArgumentException("Cette range n'est pas dénombrable");
        }
    }
}
