package analyzor.modele.berkeley;

import com.sleepycat.je.*;
import java.io.IOException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * enregistre les clés de licence et clés machine
 * les clés sont enregistrées de manière cryptée dans la base
 * avec une clé AES auto-générée et stockée ailleurs
 */
public class EnregistrementLicence extends BerkeleyDB {
    // todo ajouter un logging ??
    // todo à refactoriser
    // todo stocker la clé secrète à un autre endroit sinon ne sert à rien
    private static final String cleEntreeLicence = "HfakgkKgeHJ";
    private static final String cleEntreeMachine = "HflfkzfHHfefHHFG";
    private static final String entreeCleSecrete = "HFIUGFHEGEGgjgrzgzgGEg";
    private final SecretKeySpec cleSecrete;
    public EnregistrementLicence() {
        try {
            cleSecrete = trouverCleSecrete();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible d'initialiser la base de licence");
        }
    }



    // return null si pas de clé
    public String getCleLicence() {
        try {
            ouvrirConnexion();

            DatabaseEntry dbKey = new DatabaseEntry(cleEntreeLicence.getBytes());
            DatabaseEntry dbValue = new DatabaseEntry();

            if (database.get(null, dbKey, dbValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                fermerConnexion();
                return decrypter(dbValue.getData());
            }

            fermerConnexion();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible de récupérer la clé");
        }
        return null;
    }

    // return null si pas de clé
    public String getCleMachine() {
        try {
            ouvrirConnexion();

            DatabaseEntry dbKey = new DatabaseEntry(cleEntreeMachine.getBytes());
            DatabaseEntry dbValue = new DatabaseEntry();

            if (database.get(null, dbKey, dbValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                fermerConnexion();
                return decrypter(dbValue.getData());
            }

            fermerConnexion();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible de récupérer la clé");
        }
        return null;
    }

    public void supprimerCles() {
        supprimerCleLicence();
        supprimerCleMachine();
    }

    private void supprimerCleMachine() {
        try {
            ouvrirConnexion();
            DatabaseEntry key = new DatabaseEntry(cleEntreeMachine.getBytes());

            database.delete(null, key);
            fermerConnexion();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer la clé dans la base de données");
        }
    }

    public void supprimerCleLicence() {
        try {
            ouvrirConnexion();
            DatabaseEntry key = new DatabaseEntry(cleEntreeLicence.getBytes());
            database.delete(null, key);
            fermerConnexion();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer la clé dans la base de données");
        }
    }


    public void enregistrerCleLicence(String cleLicence) {
        try {
            ouvrirConnexion();
            byte[] cleCryptee = encrypter(cleLicence);
            DatabaseEntry key = new DatabaseEntry(cleEntreeLicence.getBytes());
            DatabaseEntry valeur = new DatabaseEntry(cleCryptee);

            database.put(null, key, valeur);
            fermerConnexion();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer la clé dans la base de données");
        }
    }

    public void enregistrerCleMachine(String cleGeneree) {
        try {
            ouvrirConnexion();
            byte[] cleCryptee = encrypter(cleGeneree);
            DatabaseEntry key = new DatabaseEntry(cleEntreeMachine.getBytes());
            DatabaseEntry valeur = new DatabaseEntry(cleCryptee);

            database.put(null, key, valeur);
            fermerConnexion();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible d'enregistrer la clé dans la base de données");
        }
    }

    private byte[] encrypter(String chaineOriginale) throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        Cipher encrypteur = Cipher.getInstance("AES");
        encrypteur.init(Cipher.ENCRYPT_MODE, cleSecrete);
        return encrypteur.doFinal(chaineOriginale.getBytes());
    }

    private String decrypter(byte[] chaineEncryptee)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        Cipher encrypteur = Cipher.getInstance("AES");
        encrypteur.init(Cipher.DECRYPT_MODE, cleSecrete);
        return new String(encrypteur.doFinal(chaineEncryptee));
    }

    private SecretKeySpec trouverCleSecrete() {
        byte[] cleSecrete = recupererCleSecreteBDD();
        if (cleSecrete != null) {
            return new SecretKeySpec(cleSecrete, "AES");
        }
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");

            // Spécifier la taille de la clé (128 bits dans ce cas)
            keyGen.init(128); // Vous pouvez également utiliser 192 ou 256 pour d'autres tailles de clé

            // Générer la clé aléatoire
            SecretKey aesKey = keyGen.generateKey();

            ouvrirConnexion();
            DatabaseEntry key = new DatabaseEntry(entreeCleSecrete.getBytes());
            DatabaseEntry value = new DatabaseEntry(aesKey.getEncoded());
            database.put(null, key, value);
            fermerConnexion();

            return new SecretKeySpec(aesKey.getEncoded(), "AES");
        }

        catch (Exception e) {
            throw new RuntimeException("Impossible de générér une clé secrète");
        }
    }

    private byte[] recupererCleSecreteBDD() {
        try {
            ouvrirConnexion();

            DatabaseEntry dbKey = new DatabaseEntry(entreeCleSecrete.getBytes());
            DatabaseEntry dbValue = new DatabaseEntry();

            if (database.get(null, dbKey, dbValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                fermerConnexion();
                return dbValue.getData();
            }

            fermerConnexion();
        }
        catch (Exception e) {
            throw new RuntimeException("Impossible de récupérer la clé");
        }
        return null;
    }

    @Override
    protected boolean ouvrirConnexion() throws IOException, DatabaseException {
        DatabaseConfig dbConfig = super.creerConfig("gen");
        //todo changer le nom on peut créer plusieurs database pour plusieurs types de données
        database = environment.openDatabase(null, "licence", dbConfig);
        return true;
    }
}
