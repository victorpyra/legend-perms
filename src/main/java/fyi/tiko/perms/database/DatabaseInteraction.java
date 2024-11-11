package fyi.tiko.perms.database;

import java.util.logging.Logger;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is used to hold the connection to the database.
 * It is used to prevent boilerplate code in the database classes.
 * Copied and changed from:
 * <a href="https://github.com/rainbowdashlabs/sql-util/blob/master/src/main/java/de/chojo/sqlutil/base/DataHolder.java">...</a>
 */
public class DatabaseInteraction {
    private final Logger logger;
    private final DataSource source;

    /**
     * Constructs a new {@link DatabaseInteraction} with the given {@link Logger} and {@link DataSource}.
     *
     * @param logger     the logger of the plugin using this holder
     * @param dataSource the data source to use
     */
    public DatabaseInteraction(Logger logger, DataSource dataSource) {
        this.logger = logger;
        this.source = dataSource;
    }

    /**
     * The {@link Logger} of the plugin that uses this holder.
     *
     * @return the logger of the plugin that uses this holder
     */
    protected Logger logger() {
        return logger;
    }

    /**
     * Attempts to establish a connection with the data source that this {@link DatabaseInteraction} contains.
     *
     * @return a new connection from the data sources
     * @throws SQLException                 if a database access error occurs
     * @throws java.sql.SQLTimeoutException when the driver has determined that the timeout value specified by the {@code setLoginTimeout} method has
     *                                      been exceeded and has at least tried to cancel the current database connection attempt
     */
    protected Connection conn() throws SQLException {
        return source.getConnection();
    }
}
