package analyzor.modele.berkeley;
import com.sleepycat.je.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BerkeleyDB {
    private static String dbPath = "prec";
    protected Database database;
    protected Environment environment;
    protected BerkeleyDB() {
    }

    protected DatabaseConfig creerConfig() throws DatabaseException, IOException {
        creerDossierBDD();
        // Créez un environnement Berkeley DB
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true); // Créez l'environnement s'il n'existe pas
        environment = new Environment(new java.io.File(dbPath), envConfig);

        // Ouvrez la base de données
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true); // Créez la base de données s'il n'existe pas

        return dbConfig;
    }

    protected void fermerConnexion() throws DatabaseException {
        if (database != null) {
                database.close();
        }
        if (environment != null) {
            environment.close();
        }
    }

    private void creerDossierBDD() throws IOException {
        Path folder = Paths.get(dbPath);

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
    }

    protected abstract boolean ouvrirConnexion() throws IOException, DatabaseException;
}
