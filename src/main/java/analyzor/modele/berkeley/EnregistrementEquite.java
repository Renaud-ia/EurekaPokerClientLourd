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

            fermerConnexion();
            return deserializeEquiteFuture(dbValue.getData());
        }

        fermerConnexion();
        return null;
    }

    private DatabaseEntry genererCle(int cleSituation, ComboIso comboIso) {

        String keyCombo = comboIso.codeReduit();


        byte[] intBytes = new byte[Integer.BYTES];
        intBytes[0] = (byte) (cleSituation >> 24);
        intBytes[1] = (byte) (cleSituation >> 16);
        intBytes[2] = (byte) (cleSituation >> 8);
        intBytes[3] = (byte) cleSituation;


        byte[] stringBytes = keyCombo.getBytes();


        byte[] keyBytes = new byte[intBytes.length + stringBytes.length];
        System.arraycopy(intBytes, 0, keyBytes, 0, intBytes.length);
        System.arraycopy(stringBytes, 0, keyBytes, intBytes.length, stringBytes.length);


        return new DatabaseEntry(keyBytes);
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

    @Override
    protected boolean ouvrirConnexion() throws IOException, DatabaseException {
        DatabaseConfig dbConfig = super.creerConfig("prec");

        database = environment.openDatabase(null, "equites", dbConfig);
        return true;
    }

}
