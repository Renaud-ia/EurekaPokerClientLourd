package analyzor.vue.donnees.format;

import analyzor.modele.parties.Variante;

import java.time.format.DateTimeFormatter;

/**
 * classe abstraite qui unifie la récupération des infos pour un format détaillé
 */
public abstract class FormFormat {
    protected DTOFormat format;

    // interface publique de consultation

    // type d'infos => vérifie si l'info a besoin d'être affichée/renseignée

    public boolean aAnte() {
        return format.getPokerFormat() == Variante.PokerFormat.MTT;
    }

    public boolean aRake() {
        return format.getPokerFormat() == Variante.PokerFormat.CASH_GAME;
    }

    public boolean bountyExiste() {
        return format.getPokerFormat() == Variante.PokerFormat.MTT;
    }


    public String getNom() {
        return format.getNomFormat();
    }
}
