package analyzor.modele.berkeley;
import com.sleepycat.je.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BerkeleyDB {
    protected Database database;
    protected Environment environment;
    protected BerkeleyDB() {
    }

    protected DatabaseConfig creerConfig(String dbPath) throws DatabaseException, IOException {
        creerDossierBDD(dbPath);

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        environment = new Environment(new java.io.File(dbPath), envConfig);


        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);

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

    private void creerDossierBDD(String dbPath) throws IOException {
        Path folder = Paths.get(dbPath);

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
    }

    protected abstract boolean ouvrirConnexion() throws IOException, DatabaseException;
}
