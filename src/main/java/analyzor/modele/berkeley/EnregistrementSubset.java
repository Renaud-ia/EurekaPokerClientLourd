package analyzor.modele.berkeley;


import analyzor.modele.poker.Board;
import com.sleepycat.je.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * permet de garder les Subsets précalculés dans la BDD
 */
public class EnregistrementSubset extends BerkeleyDB {
    // pour coder max 1755 valeurs, on a besoin de 11 bits
    private final static int N_BITS_SUBSETS = 11;

    /**
     * interface publique pour enregistrer les Subsets
     * @param boardsCorrespondants listes des boards qui composent le subset
     */
    public void enregistrerSubsets(List<Board> boardsCorrespondants)
            throws IOException, DatabaseException {
        int nombreSubsets = boardsCorrespondants.size();
        ouvrirConnexion();
        for (int i = 0; i < boardsCorrespondants.size(); i++) {
            DatabaseEntry cle = obtenirCle(nombreSubsets, i);
            int boardInt = boardsCorrespondants.get(i).asInt();
            DatabaseEntry valeur = obtenirValeur(boardInt);

            database.put(null, cle, valeur);
        }
        fermerConnexion();
    }

    /**
     *
     * @param nombreSubsets taille du subset cherché
     * @return la liste de subsets la plus proche trouvée si nombre spécifié n'est pas trouvé
     */
    public List<Board> recupererSubsets(int nombreSubsets)
            throws IOException, DatabaseException {
        ouvrirConnexion();
        // on va chercher de manière récursive le subset le plus proche
        int i = 0;
        while (((nombreSubsets - i) > 0) && ((nombreSubsets + i) < 1755)) {
            int cleCherchee = nombreSubsets - i;
            if (subsetExiste(cleCherchee)) {
                return trouverBoards(nombreSubsets);
            }
            cleCherchee = nombreSubsets + i;
            if (subsetExiste(cleCherchee)) {
                return trouverBoards(cleCherchee);
            }
            i++;
        }

        throw new RuntimeException("Aucun subset n'est enregistré dans la BDD");
    }

    private List<Board> trouverBoards(int nombreSubsets)
            throws DatabaseException {
        List<Board> boardsSubsets = new ArrayList<>();

        int i = 0;
        Board boardTrouve;
        while((boardTrouve = trouverBoard(nombreSubsets, i)) != null) {
            boardsSubsets.add(boardTrouve);
            i++;
        }

        return boardsSubsets;
    }

    private Board trouverBoard(int nombreSubsets, int index)
            throws DatabaseException {
        DatabaseEntry cle = obtenirCle(nombreSubsets, index);
        DatabaseEntry valeur = new DatabaseEntry();

        if (database.get(null, cle, valeur, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int boardInt = bytesToInt(valeur.getData());
            return new Board(boardInt);
        }

        else return null;
    }

    private boolean subsetExiste(int nombreSubsets)
            throws DatabaseException {
        if (nombreSubsets < 0) return false;
        if (nombreSubsets > 1755) return false;
        DatabaseEntry premiereCle = obtenirCle(nombreSubsets, 0);
        DatabaseEntry valeur = new DatabaseEntry();

        return database.get(null, premiereCle, valeur, LockMode.DEFAULT) == OperationStatus.SUCCESS;

    }

    private DatabaseEntry obtenirValeur(int boardInt) {
        return new DatabaseEntry(intToBytes(boardInt));
    }

    private DatabaseEntry obtenirCle(int nombreSubsets, int i) {
        int index = (nombreSubsets << N_BITS_SUBSETS) + i;
        return new DatabaseEntry(intToBytes(index));
    }

    private static byte[] intToBytes(int valeur) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(valeur).array();
    }

    private static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    @Override
    protected boolean ouvrirConnexion() throws IOException, DatabaseException {
        DatabaseConfig dbConfig = super.creerConfig("prec");
        //todo changer le nom on peut créer plusieurs database pour plusieurs types de données
        database = environment.openDatabase(null, "subsets", dbConfig);
        return true;
    }
}
