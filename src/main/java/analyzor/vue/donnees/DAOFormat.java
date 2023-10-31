package analyzor.vue.donnees;

import java.util.*;

/**
 * interface entre controleur et vue pour affichage des données
 * ne permet de modifier que les éléments graphiques qui ont changé
 */
public class DAOFormat {
    private int indexActuel;
    private final HashMap<Integer, InfosFormat> infosFormats;
    private final HashSet<Integer> nouveauxFormats;
    private final HashSet<Integer> formatsSupprimes;
    private final HashSet<Integer> formatsModifies;

    public DAOFormat() {
        indexActuel = 0;
        infosFormats = new HashMap<>();
        nouveauxFormats = new HashSet<>();
        formatsSupprimes = new HashSet<>();
        formatsModifies = new HashSet<>();
    }


    // interface controleur
    public void ajouterFormat(
            Long idBDD,
            String nomFormat,
            boolean ante,
            boolean ko,
            int nJoueurs,
            float minBuyIn,
            float maxBuyIn,
            int nombreParties,
            int nouvellesParties,
            boolean preflopCalcule,
            boolean flopCalcule
    ) {
        InfosFormat nouveauFormat = new InfosFormat(
                idBDD,
                indexActuel,
                nomFormat,
                ante,
                ko,
                nJoueurs,
                minBuyIn,
                maxBuyIn,
                nombreParties,
                nouvellesParties,
                preflopCalcule,
                flopCalcule
        );
        infosFormats.put(indexActuel, nouveauFormat);
        nouveauxFormats.add(indexActuel);
        indexActuel++;
    }

    public void supprimerFormat(int index) {
        formatsSupprimes.add(index);
    }

    public void setPreflopResolu(int index) {
        infosFormats.get(index).preflopCalcule = true;
        formatsModifies.add(index);
    }

    public void setFlopResolu(int index) {
        infosFormats.get(index).flopCalcule = true;
        formatsModifies.add(index);
    }

    public Long getIdBDD(int index) {
        return infosFormats.get(index).idBDD;
    }

    public void setNombreParties(int index, int nombreParties) {
        infosFormats.get(index).nombreParties = nombreParties;
        formatsModifies.add(index);
    }

    public void setNouvellesParties(int index, int nouvellesParties) {
        infosFormats.get(index).nouvellesParties = nouvellesParties;
        formatsModifies.add(index);
    }

    // interface vue
    public List<InfosFormat> nouveauxFormats() {
        List<InfosFormat> listFormats = new ArrayList<>();
        for (int index : this.nouveauxFormats) {
            listFormats.add(this.infosFormats.get(index));
        }
        this.nouveauxFormats.clear();
        return listFormats;
    }
    public List<InfosFormat> formatModifies() {
        List<InfosFormat> listFormats = new ArrayList<>();
        for (int index : this.formatsModifies) {
            listFormats.add(this.infosFormats.get(index));
        }
        this.formatsModifies.clear();
        return listFormats;
    }

    /**
     * indique les formats supprimés et les supprime dans la foulée
     */
    public Set<Integer> formatsSupprimes() {
        HashSet<Integer> listeRetournee = new HashSet<>(this.formatsSupprimes);
        this.formatsSupprimes.clear();
        return listeRetournee;
    }

    public class InfosFormat {
        //on garde une correspondance pour suppression

        // attributs non modifiables
        private Long idBDD;
        private final int indexAffichage;
        private final String nomFormat;
        private final boolean ante;
        private final boolean ko;
        private final int nJoueurs;
        private final float minBuyIn;
        private final float maxBuyIn;

        // attributs modifiables
        private int nombreParties;
        private int nouvellesParties;
        private boolean preflopCalcule;
        private boolean flopCalcule;

        private InfosFormat(
                Long idBDD,
                int indexAffichage,
                String nomFormat,
                boolean ante,
                boolean ko,
                int nJoueurs,
                float minBuyIn,
                float maxBuyIn,
                int nombreParties,
                int nouvellesParties,
                boolean preflopCalcule,
                boolean flopCalcule
        ) {
            this.idBDD = idBDD;
            this.indexAffichage = indexAffichage;
            this.nomFormat = nomFormat;
            this.ante = ante;
            this.ko = ko;
            this.nJoueurs = nJoueurs;
            this.minBuyIn = minBuyIn;
            this.maxBuyIn = maxBuyIn;
            this.nombreParties = nombreParties;
            this.nouvellesParties = nouvellesParties;
            this.preflopCalcule = preflopCalcule;
            this.flopCalcule = flopCalcule;
        }

        //interface de consultation pour affichage
        //pas de modification possible

        public int getIndexAffichage() {
            return indexAffichage;
        }

        public String getNomFormat() {
            return nomFormat;
        }

        public boolean isAnte() {
            return ante;
        }

        public boolean isKo() {
            return ko;
        }

        public int getnJoueurs() {
            return nJoueurs;
        }

        public float getMinBuyIn() {
            return minBuyIn;
        }

        public float getMaxBuyIn() {
            return maxBuyIn;
        }

        public boolean isPreflopCalcule() {
            return preflopCalcule;
        }

        public boolean isFlopCalcule() {
            return flopCalcule;
        }

        public int getNombreParties() {
            return nombreParties;
        }

        public String getEtat() {
            if (nombreParties == 0) {
                return "Aucune partie correspondante";
            }
            else if (preflopCalcule && flopCalcule) {
                int pctMains = (int) nouvellesParties / nombreParties;
                return "Calcul\u00E9 sur " + pctMains + "% des parties";
            }
            else if (preflopCalcule) {
                return "Flop non calcul\u00E9";
            }
            else return "Non calcul\u00E9";
        }

        public boolean selectionnable() {
            return preflopCalcule;
        }

        public Long getIdBDD() {
            return idBDD;
        }
    }
}
