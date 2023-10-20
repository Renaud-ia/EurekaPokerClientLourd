package analyzor.vue.donnees;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InfosRoom {
    private final Room[] rooms;
    public InfosRoom(int nRooms) {
        rooms = new Room[nRooms];
    }
    public void ajouterDossier(int index, String nomDossier, int nombreFichiers) {
        rooms[index].addDossier(nomDossier, nombreFichiers);
    }

    public void setRoom(int index,
                        String nomRoom,
                        int nMains,
                        boolean etat) {
        rooms[index] = new Room(nomRoom);
        modifierRoom(index, nMains, etat);
    }

    public void modifierRoom (int index,
                              int nMains,
                              boolean etat) {
        rooms[index].setNMains(nMains);
        rooms[index].setEtat(etat);
    }

    public String[] getDossiers(int index) {
        return rooms[index].getDossiers();
    }

    public Object[] getDonneesRooms(int index) {
        Room room = rooms[index];
        String[] donneesLigne;
        donneesLigne = new String[]{
                                    room.getNom(),
                                    String.valueOf(room.getNParties()),
                                    String.valueOf(room.getNMains()),
                                    String.valueOf(room.getNDossiers()),
                                    room.getEtat()
        };
        return donneesLigne;
    }

    public int nRooms() {
        return rooms.length;
    }

    public int getNParties(int indexRoom, String dossier) {
        return rooms[indexRoom].getNParties(dossier);
    }

    public int getTotalParties() {
        int nParties = 0;
        for (Room room: rooms) {
            nParties += room.getNParties();
        }
        return nParties;
    }

    public String nomRoom(int indexRoom) {
        return rooms[indexRoom].getNom();
    }

    public String etatRoom(int indexRoom) {
        return rooms[indexRoom].getEtat();
    }

    public void supprimerDossiers(int indexRoom) {
        rooms[indexRoom].clearDossiers();
    }


    private class Room {
        private final String nom;
        private int nMains;
        private final List<Dossier> dossiers = new ArrayList<>();
        private boolean etat;

        protected Room(String nom) {
            this.nom = nom;
        }

        protected void addDossier(String nomDossier, int nombreFichiers) {
            dossiers.add(new Dossier(nomDossier, nombreFichiers));
        }

        protected void setNMains(int nMains) {
            this.nMains = nMains;
        }

        protected void setEtat(boolean etat) {
            this.etat = etat;
        }

        protected String getNom() {
            return nom;
        }

        protected int getNParties() {
            int nParties = 0;
            for (Dossier dossier : dossiers) {
                nParties += dossier.getNombreFichiers();
            }
            return nParties;
        }

        protected int getNParties(String nomDossier) {
            for (Dossier dossier : dossiers) {
                if (Objects.equals(dossier.nom, nomDossier)) return dossier.getNombreFichiers();
            }
            return 0;
        }

        protected int getNMains() {
            return nMains;
        }

        protected int getNDossiers() {
            return dossiers.size();
        }

        protected String getEtat() {
            return (etat) ? "Active" : "Non active";
        }

        protected String[] getDossiers() {
            String[] nomsDossiers = new String[dossiers.size()];

            for (int i = 0; i < dossiers.size(); i++) {
                nomsDossiers[i] = dossiers.get(i).getNom();
            }
            return nomsDossiers;
        }

        public void clearDossiers() {
            dossiers.clear();
        }

        private class Dossier {
            private final String nom;
            private final int nombreFichiers;

            public Dossier(String nom, int nombreFichiers) {
                this.nom = nom;
                this.nombreFichiers = nombreFichiers;
            }

            public String getNom() {
                return nom;
            }

            public int getNombreFichiers() {
                return nombreFichiers;
            }
        }
    }

}
