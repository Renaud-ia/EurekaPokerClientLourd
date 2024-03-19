package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.Variante;

import java.time.LocalDateTime;

public class DTOLecteurWinamax extends DTOLecteurTxt {
    private enum FormatWinamax {
        CASH_GAME,
        MTT,
        EXPRESSO,
        ESCAPE,
        FLOOP,
        SHORT_TRACK,
        INCONNU
    }

    /**
     * classe qui centralise et gère les informations sur la table
     */
    public static class InfosPartie  {
        final float WINAMAX_ANTE = 12.5f;
        final float WINAMAX_RAKE = 5.75f;
        private InfosFormat infosFormat;
        private InfosTable infosTable;
        private FormatWinamax formatWinamax;
        private boolean bounty;
        private String nomHero;
        private float rake;

        public InfosPartie() {
            rake = 0f;
        }

        // procédure pour déterminer un format

        private void determinerFormatWinamax() {
            if (getFormatPoker() == Variante.PokerFormat.SPIN) {
                formatWinamax = FormatWinamax.EXPRESSO;
            }

            else if (getFormatPoker() == Variante.PokerFormat.MTT) {
                if (infosFormat.getAnte() > 0) {
                    formatWinamax = FormatWinamax.MTT;
                }

                else {
                    System.out.println("ANTE = 0");
                    formatWinamax = FormatWinamax.INCONNU;
                }
            }

            else if (getFormatPoker() == Variante.PokerFormat.CASH_GAME) {
                //
                if (infosTable.estShortTrack()) {
                    formatWinamax = FormatWinamax.SHORT_TRACK;
                }
                // todo ajouter escape et FLOOP et HOLD UP et GO FAST et SNG + SNG Deglingos

                else {
                    formatWinamax = FormatWinamax.CASH_GAME;
                }
            }

            else {
                // go fast n'est pas Cash Game donc sera dans cette catégorie
                formatWinamax = FormatWinamax.INCONNU;
            }
        }

        // setters

        public void setInfosFormat(InfosFormat infosFormat) {
            this.infosFormat = infosFormat;
        }

        public void setInfosTable(InfosTable infosTable) {
            this.infosTable = infosTable;

            determinerFormatWinamax();
        }

        public void setBounty(boolean bounty) {
            this.bounty = bounty;
        }
        public void setNomHero(String nomHero) {
            this.nomHero = nomHero;
        }

        public void setRake(float rake) {
            this.rake = rake;
        }

        // getters avec traitement spécial

        public float getAnte() {
            if (formatWinamax == FormatWinamax.MTT) {
                return WINAMAX_ANTE;
            }
            return 0f;
        }

        public float getRake() {
            if (formatWinamax == FormatWinamax.CASH_GAME) {
                return WINAMAX_RAKE;
            }
            return 0f;
        }

        public boolean formatPrisEnCharge() {
            return (formatWinamax != null &&
                    (formatWinamax == FormatWinamax.EXPRESSO
                            || formatWinamax == FormatWinamax.CASH_GAME
                            || formatWinamax == FormatWinamax.MTT)
            );
        }

        // getters normaux

        public Variante.PokerFormat getFormatPoker() {
            return infosFormat.getFormatPoker();
        }

        public Variante.VariantePoker getVariantePoker() {
            return infosFormat.getVariantePoker();
        }

        public float getBuyIn() {
            return infosFormat.getBuyIn();
        }

        public int getNombreJoueurs() {
            return infosTable.getNombreJoueurs();
        }

        public boolean getBounty() {
            return bounty;
        }

        public Long getNumeroTable() {
            return infosFormat.getNumeroTable();
        }

        public String getNomHero() throws InformationsIncorrectes {
            if (nomHero == null) throw new InformationsIncorrectes("Nom hero non trouvé");
            return nomHero;
        }

        public String getNomTable() {
            return infosTable.getNomTable();
        }

        public LocalDateTime getDate() {
            return infosFormat.getDate();
        }

        public String getNomFormat() {
            return formatWinamax.toString();
        }
    }
    // stocke les infos du format = première ligne
    public static class InfosFormat {
        private final Variante.VariantePoker variantePoker;
        private final Variante.PokerFormat pokerFormat;
        private final float buyIn;
        private final LocalDateTime date;
        private final long numeroTable;
        private final long numeroMain;
        private final float ante;
        private float rake;

        public InfosFormat(Variante.VariantePoker variantePoker,
                           Variante.PokerFormat pokerFormat,
                           float buyIn,
                           LocalDateTime date,
                           long numeroTable,
                           long numeroMain,
                           float ante,
                           float rake) {

            this.variantePoker = variantePoker;
            this.pokerFormat = pokerFormat;
            this.buyIn = buyIn;
            this.date = date;
            this.numeroTable = numeroTable;
            this.numeroMain = numeroMain;
            this.ante = ante;
            this.rake = rake;
        }

        public Variante.VariantePoker getVariantePoker() {
            return variantePoker;
        }

        public Variante.PokerFormat getFormatPoker() {
            return pokerFormat;
        }

        public float getBuyIn() {
            return buyIn;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public long getNumeroTable() {
            return numeroTable;
        }

        public long getNumeroMain() {
            return numeroMain;
        }

        public void setRake(float rake) {
            this.rake = rake;
        }

        public float getAnte() {
            return ante;
        }

        public float getRake() {
            return rake;
        }
    }


    public static class InfosTable {
        // seconde ligne, on leve une exception si short track, on récupère le nom de la table, le nombre de joueurs
        private final String nomTable;
        private final int nombreJoueurs;

        public InfosTable(String nomTable, int nombreJoueurs) {
            this.nomTable = nomTable;
            this.nombreJoueurs = nombreJoueurs;
        }

        public boolean estShortTrack() {
            return nomTable.contains("SHORT TRACK");
        }

        public String getNomTable() {
            return nomTable;
        }

        public int getNombreJoueurs() {
            return nombreJoueurs;
        }
    }

}
