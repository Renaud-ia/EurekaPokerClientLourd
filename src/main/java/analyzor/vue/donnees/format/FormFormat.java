package analyzor.vue.donnees.format;

import analyzor.modele.parties.Variante;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public abstract class FormFormat {
    protected DTOFormat format;

    

    

    public boolean aAnte() {
        return format.getPokerFormat() == Variante.PokerFormat.MTT;
    }

    public boolean aRake() {
        return format.getPokerFormat() == Variante.PokerFormat.CASH_GAME;
    }

    public boolean bountyExiste() {
        
        return false;
        
    }


    public String getNom() {
        return format.getNomFormat();
    }

    public String getDateMinimum() {
        LocalDateTime joueApres = format.getJoueApres();
        return convertirDateEnString(joueApres);
    }

    public String getDateMaximum() {
        LocalDateTime joueAvant = format.getJoueAvant();
        return convertirDateEnString(joueAvant);
    }

    private String convertirDateEnString(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return date.format(formatter);
    }
}
