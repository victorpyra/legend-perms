package fyi.tiko.perms.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.jetbrains.annotations.Nullable;

/**
 * Used to connect to the database.
 */
public class DatabaseProvider {

    private final String host;
    private final String user;
    private final String password;
    private final String database;
    private final int port;
    private HikariDataSource source;

    /**
     * Default constructor of the DatabaseProvider. This constructor is private and should only be used by the create method.
     */
    private DatabaseProvider(String host, String user, String password, String database, int port) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
        this.port = port;
    }

    /**
     * Creates a new instance of the DatabaseProvider.
     *
     * @param host     The host of the database.
     * @param user     The username of the database.
     * @param password The password of the database.
     * @param database The name of the database.
     * @param port     The port of the database.
     * @return A new instance of the DbProvider.
     */
    public static DatabaseProvider create(String host, String user, String password, String database, int port) {
        return new DatabaseProvider(host, user, password, database, port);
    }

    /**
     * Connects to the database and returns true if the connection was successful
     */
    public boolean connect() {
        var config = new HikariConfig();

        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, database));
        config.setMaximumPoolSize(10);
        config.setUsername(user);
        config.setPassword(password);

        source = new HikariDataSource(config);
        return connected();
    }

    /**
     * Attempts to disconnect from the database
     */
    public void disconnect() {
        source.close();
    }

    /**
     * Checks if the connection is established
     *
     * @return true if the connection is established, false otherwise
     */
    public boolean connected() {
        return source != null;
    }

    /**
     * @return the data source, null if the connection failed
     */
    public @Nullable HikariDataSource dataSource() {
        return source;
    }
}
