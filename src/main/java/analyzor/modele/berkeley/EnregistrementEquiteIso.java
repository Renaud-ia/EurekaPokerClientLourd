package analyzor.modele.berkeley;

import analyzor.modele.poker.ComboIso;
import analyzor.modele.poker.evaluation.EquiteFuture;
import com.sleepycat.je.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EnregistrementEquiteIso extends BerkeleyDB {
    public EnregistrementEquiteIso() {
        super();
    }

    public void enregistrerCombo(ComboIso comboIso, EquiteFuture equiteFuture)
            throws DatabaseException, IOException {
        ouvrirConnexion();
        DatabaseEntry key = genererCle(comboIso);
        DatabaseEntry valeur = serialiserEquite(equiteFuture);

        database.put(null, key, valeur);
        fermerConnexion();
    }
    public EquiteFuture recupererEquite(ComboIso comboIso) throws
            DatabaseException, IOException, ClassNotFoundException {
        ouvrirConnexion();

        DatabaseEntry dbKey = genererCle(comboIso);
        DatabaseEntry dbValue = new DatabaseEntry();

        if (database.get(null, dbKey, dbValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            // Désérialisez les données en un objet EquiteFuture
            fermerConnexion();
            return deserializeEquiteFuture(dbValue.getData());
        }

        fermerConnexion();
        return null;
    }

    private DatabaseEntry genererCle(ComboIso comboIso) {
        String key = comboIso.codeReduit();
        return new DatabaseEntry(key.getBytes(StandardCharsets.UTF_8));
    }

    private DatabaseEntry serialiserEquite(EquiteFuture equiteFuture) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(equiteFuture);

        return new DatabaseEntry(bos.toByteArray());
    }

    private EquiteFuture deserializeEquiteFuture(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);

        return (EquiteFuture) ois.readObject();
    }

}
