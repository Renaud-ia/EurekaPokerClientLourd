package analyzor.vue.donnees;

public class InfosRoom {
    private Room[] rooms;
    private int index = 0;
    public InfosRoom(int nRooms) {
        rooms = new Room[nRooms];
    }
    public void ajouterDossier(int index, String nomDossier, int nombreFichiers) {
        rooms[index].addDossier(nomDossier, nombreFichiers);
    }

    public void setRoom(int index,
                        String nomRoom,
                        String detailRoom,
                        int nParties,
                        int nMains,
                        int nDossiers,
                        boolean etat) {
        rooms[index] = new Room(nomRoom, detailRoom);
        modifierRoom(index, nParties, nMains, nDossiers, etat);
    }

    public void modifierRoom (int index,
                              int nParties,
                              int nMains,
                              int nDossiers,
                              boolean etat) {
        rooms[index].setNParties(nParties);
        rooms[index].setNMains(nMains);
        rooms[index].setNDossiers(nDossiers);
        rooms[index].setEtat(etat);
    }

    public void ajouterDossier (String nomDossier, int nombreFichiers) {
        //todo
    }

    public Object[] getDonneesRooms(int index) {
        Room room = rooms[index];
        Object[] donneesLigne = new Object[]{room.getNom(), room.getNParties()};
        return donneesLigne;
    }

    public int nRooms() {
        return rooms.length;
    }


    private class Room {
        private final String nom;
        private final String detail;
        private int nParties;
        private int nMains;
        private int nDossiers;
        private boolean etat;

        protected Room(String nom, String detail) {
            this.nom = nom;
            this.detail = detail;
        }

        protected void addDossier(String nomDossier, int nombreFichiers) {
        }

        protected void setNParties(int nParties) {
            this.nParties = nParties;
        }

        public void setNMains(int nMains) {
        }

        public void setNDossiers(int nDossiers) {
        }

        public void setEtat(boolean etat) {
        }

        public String getNom() {
            String nomAffiche = this.nom;
            if (detail.length() > 0) nomAffiche += "(" + this.detail + ")";
            return nomAffiche;
        }

        public int getNParties() {
            return 0;
        }

        private class Dossiers {

        }

    }

}
