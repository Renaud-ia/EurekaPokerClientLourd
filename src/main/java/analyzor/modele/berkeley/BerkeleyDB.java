package analyzor.modele.berkeley;
import com.sleepycat.je.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BerkeleyDB {
    private static String dbPath = "berkeleydb";
    protected Database database;
    protected Environment environment;
    protected BerkeleyDB() {
    }

    protected void ouvrirConnexion() throws DatabaseException {
        // Créez un environnement Berkeley DB
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true); // Créez l'environnement s'il n'existe pas
        environment = new Environment(new java.io.File(dbPath), envConfig);

        // Ouvrez la base de données
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true); // Créez la base de données s'il n'existe pas
        database = environment.openDatabase(null, "mydatabase", dbConfig);
    }

    protected void fermerConnexion() throws DatabaseException {
        if (database != null) {
                database.close();
        }
        if (environment != null) {
            environment.close();
        }
    }
}
