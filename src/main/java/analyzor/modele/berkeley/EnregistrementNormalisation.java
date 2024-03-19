package analyzor.modele.berkeley;

import analyzor.modele.clustering.objets.MinMaxCalcul;
import analyzor.modele.clustering.objets.MinMaxCalculSituation;
import analyzor.modele.simulation.SituationStackPotBounty;
import com.sleepycat.je.*;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * garde en mémoire les
 */
public class EnregistrementNormalisation extends BerkeleyDB {
    public EnregistrementNormalisation() {
        super();
    }

    public void enregistrerMinMax(long cleFormatBDD,
                                 long idNoeudTheorique,
                                  MinMaxCalculSituation minMaxCalculSituation)
            throws DatabaseException, IOException {

        ouvrirConnexion();
        DatabaseEntry key = genererCle(cleFormatBDD, idNoeudTheorique);
        DatabaseEntry valeur = serialiserMinMax(minMaxCalculSituation);

        database.put(null, key, valeur);
        fermerConnexion();
    }

    public MinMaxCalculSituation recupererMinMax(long cleFormatBDD,
                                                                 long idNoeudTheorique) throws
            DatabaseException, IOException, ClassNotFoundException {
        ouvrirConnexion();

        DatabaseEntry dbKey = genererCle(cleFormatBDD, idNoeudTheorique);
        DatabaseEntry dbValue = new DatabaseEntry();

        if (database.get(null, dbKey, dbValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            // Désérialisez les données en un objet EquiteFuture
            fermerConnexion();
            return deserialiserMinMax(dbValue.getData());
        }

        fermerConnexion();
        throw new DatabaseException("Clé Min Max non trouvée");
    }

    private DatabaseEntry genererCle(long cleFormatBDD, long idNoeudTheorique) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES * 2);
        byteBuffer.putLong(cleFormatBDD);
        byteBuffer.putLong(idNoeudTheorique);

        return new DatabaseEntry(byteBuffer.array());
    }

    private DatabaseEntry serialiserMinMax(MinMaxCalculSituation minMax) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(minMax);

        return new DatabaseEntry(bos.toByteArray());
    }

    private MinMaxCalculSituation deserialiserMinMax(byte[] data) throws IOException, ClassNotFoundException {
        //todo PRODUCTION gérer l'exception om l'objet a change => dans ce cas on veut ERASE LA BDD correspondante
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);

        return (MinMaxCalculSituation) ois.readObject();
    }


    @Override
    protected boolean ouvrirConnexion() throws IOException, DatabaseException {
        DatabaseConfig dbConfig = super.creerConfig("gen");
        //todo changer le nom on peut créer plusieurs database pour plusieurs types de données
        database = environment.openDatabase(null, "normalisation", dbConfig);
        return true;
    }
}
