package analyzor.modele.berkeley;

import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.EquiteFuture;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EnregistrementEquite extends BerkeleyDB {
    public EnregistrementEquite() {
        super();
    }

    public void enregistrerCombo(int cleSituation, ComboIso comboIso, EquiteFuture equiteFuture)
            throws DatabaseException, IOException {
        // todo ajouter le nombre de joueurs??
        ouvrirConnexion();
        DatabaseEntry key = genererCle(cleSituation, comboIso);
        DatabaseEntry valeur = serialiserEquite(equiteFuture);

        database.put(null, key, valeur);
        fermerConnexion();
    }
    public EquiteFuture recupererEquite(int cleSituation, ComboIso comboIso) throws
            DatabaseException, IOException, ClassNotFoundException {
        ouvrirConnexion();

        DatabaseEntry dbKey = genererCle(cleSituation, comboIso);
        DatabaseEntry dbValue = new DatabaseEntry();

        if (database.get(null, dbKey, dbValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            // Désérialisez les données en un objet EquiteFuture
            fermerConnexion();
            return deserializeEquiteFuture(dbValue.getData());
        }

        fermerConnexion();
        return null;
    }

    private DatabaseEntry genererCle(int cleSituation, ComboIso comboIso) {
        // todo sérialiser l'objet pour ne pas dépendre de code réduit?? => problème avec hibernate
        String keyCombo = comboIso.codeReduit();

        // Convertir l'entier en tableau de bytes
        byte[] intBytes = new byte[Integer.BYTES];
        intBytes[0] = (byte) (cleSituation >> 24);
        intBytes[1] = (byte) (cleSituation >> 16);
        intBytes[2] = (byte) (cleSituation >> 8);
        intBytes[3] = (byte) cleSituation;

        // Convertir la chaîne en tableau de bytes
        byte[] stringBytes = keyCombo.getBytes();

        // Créer un tableau de bytes pour la clé en combinant les deux
        byte[] keyBytes = new byte[intBytes.length + stringBytes.length];
        System.arraycopy(intBytes, 0, keyBytes, 0, intBytes.length);
        System.arraycopy(stringBytes, 0, keyBytes, intBytes.length, stringBytes.length);

        // Créer une DatabaseEntry pour stocker la clé
        return new DatabaseEntry(keyBytes);
    }

    private DatabaseEntry serialiserEquite(EquiteFuture equiteFuture) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(equiteFuture);

        return new DatabaseEntry(bos.toByteArray());
    }

    private EquiteFuture deserializeEquiteFuture(byte[] data) throws IOException, ClassNotFoundException {
        //todo PRODUCTION gérer l'exception om l'objet a change => dans ce cas on veut ERASE LA BDD correspondante
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);

        return (EquiteFuture) ois.readObject();
    }

    private boolean ouvrirConnexion() throws IOException, DatabaseException {
        DatabaseConfig dbConfig = super.creerConfig();
        //todo changer le nom on peut créer plusieurs database pour plusieurs types de données
        database = environment.openDatabase(null, "equites", dbConfig);
        return true;
    }

}
