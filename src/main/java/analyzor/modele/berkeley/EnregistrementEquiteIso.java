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
            throws DatabaseException, IOException, ConnexionFermeeBerkeley {
        // todo ajouter le nombre de joueurs??
        ouvrirConnexion();
        DatabaseEntry key = genererCle(comboIso);
        DatabaseEntry valeur = serialiserEquite(equiteFuture);

        database.put(null, key, valeur);
        fermerConnexion();
    }
    public EquiteFuture recupererEquite(ComboIso comboIso) throws
            DatabaseException, IOException, ClassNotFoundException, ConnexionFermeeBerkeley {
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
        // todo sérialiser l'objet pour ne pas dépendre de code réduit?? => problème avec hibernate
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
        //todo PRODUCTION gérer l'exception om l'objet a change => dans ce cas on veut ERASE LA BDD correspondante
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);

        return (EquiteFuture) ois.readObject();
    }

    private boolean ouvrirConnexion() throws IOException, DatabaseException, ConnexionFermeeBerkeley {
        if (!connexionPossible) throw new ConnexionFermeeBerkeley();

        DatabaseConfig dbConfig = super.creerConfig();
        //todo changer le nom on peut créer plusieurs database pour plusieurs types de données
        database = environment.openDatabase(null, "mydatabase", dbConfig);
        return true;
    }

}
