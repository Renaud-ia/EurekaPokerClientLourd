package analyzor.vue.donnees.format;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Variante;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * interface pour affichage détaillé d'un format dans vue et modification des champs privés
 */
public class FormCreationFormat extends FormFormat {
    /**
     * création d'un formulaire vide
     * @param nomFormat le type de format qu'on va créer sous forme de String
     */
    public FormCreationFormat(String nomFormat) {
        if (Objects.equals(nomFormat, Variante.PokerFormat.SPIN.toString())) {
            format = new DTOFormat(
                    Variante.PokerFormat.SPIN,
                    LocalDateTime.now(),
                    3
            );
        }

        else if (Objects.equals(nomFormat, Variante.PokerFormat.MTT.toString())) {
            format = new DTOFormat(
                    Variante.PokerFormat.MTT,
                    LocalDateTime.now(),
                    6
            );
        }

        else if (Objects.equals(nomFormat, Variante.PokerFormat.CASH_GAME.toString())) {
            format = new DTOFormat(
                    Variante.PokerFormat.CASH_GAME,
                    LocalDateTime.now(),
                    6
            );
        }

        else throw new IllegalArgumentException("Format indisponible");
    }

    public DTOFormat getFormat() {
        return format;
    }

    // interface publique de modification

    public void setNombreJoueurs(int valeurSlider) {
        this.format.setNombreJoueurs(valeurSlider);
    }

    public void setMinAnte(int valeurSlider) {
        this.format.setMinAnte(valeurSlider);
    }

    public void setMaxAnte(int valeurSlider) {
        this.format.setMaxAnte(valeurSlider);
    }

    public void setMinRake(int valeurSlider) {
        this.format.setMinRake(valeurSlider);
    }

    public void setMaxRake(int valeurSlider) {
        format.setMaxRake(valeurSlider);
    }

    public void setBounty(boolean etat) {
        format.setBounty(etat);
    }

    public void setNomFormat(String valeur) {
        format.setNom(valeur);
    }

    public void setMinBuyIn(int valeurSlider) {
        format.setMinBuyIn(valeurSlider);
    }

    public void setMaxBuyIn(int valeurSlider) {
        format.setMaxBuyIn(valeurSlider);
    }


    public void setDateMinimum(String valeurChampSaisie) {
        this.format.setDateMinimum(convertirStringEnDate(valeurChampSaisie));
    }

    public void setDateMaximum(String valeurChampSaisie) {
        this.format.setDateMaximum(convertirStringEnDate(valeurChampSaisie));
    }

    private LocalDate convertirStringEnDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Convertir la chaîne en LocalDate
        return LocalDate.parse(dateString, formatter);
    }
}
