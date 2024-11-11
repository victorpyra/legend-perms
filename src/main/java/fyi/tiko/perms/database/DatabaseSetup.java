package fyi.tiko.perms.database;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * This class initializes the database. Copied and changed from: <a
 * href="https://github.com/rainbowdashlabs/sql-java/blob/1701279480c9c82b05b30e40a3348e6ec0263381/src/main/java/de/chojo/simplecoins/data/util/DbSetup.java#L13">...</a>
 */
public class DatabaseSetup {

    private DatabaseSetup() throws InstantiationException {
        throw new InstantiationException("This class is not meant to be instantiated");
    }

    /**
     * Reads the dbsetup.sql file from the resources folder and executes the given queries.
     *
     * @param logger the logger to log to
     */
    public static void executeQueries(Logger logger, DataSource source) {
        String setup = null;

        try (var inputStream = DatabaseSetup.class.getClassLoader().getResourceAsStream("dbsetup.sql")) {
            setup = new String(inputStream.readAllBytes());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read dbsetup.sql file.", e);
        }
        var queries = setup.split(";");

        try (var conn = source.getConnection()) {
            conn.setAutoCommit(false);
            for (var query : queries) {
                if (query.isBlank()) {
                    continue;
                }
                try (var stmt = conn.prepareStatement(query)) {
                    logger.info("Executing query: " + query);
                    stmt.execute();
                }
                conn.commit();
            }
            logger.info("Database setup complete.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not execute query.", e);
        }
    }
}
