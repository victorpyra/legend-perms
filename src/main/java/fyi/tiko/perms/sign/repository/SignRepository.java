package fyi.tiko.perms.sign.repository;

import fyi.tiko.perms.database.DatabaseInteraction;
import fyi.tiko.perms.sign.PermissionSign;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Used to save and load {@link PermissionSign}s from the database.
 *
 * @author tiko
 */
public class SignRepository extends DatabaseInteraction {

    /**
     * Constructs a new {@link DatabaseInteraction} with the given {@link Logger} and {@link DataSource}.
     *
     * @param logger the logger of the plugin using this holder
     * @param source the data source to use
     */
    public SignRepository(Logger logger, DataSource source) {
        super(logger, source);
    }

    /**
     * Deletes the given sign from the database.
     *
     * @param sign The sign to delete.
     */
    public void deleteSign(PermissionSign sign) {
        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM permission_signs WHERE id= ?")) {
            stmt.setInt(1, sign.id());
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.SEVERE, "Failed to delete sign from the database.", exception);
        }
    }

    /**
     * Adds a sign to the database.
     *
     * @param location The location of the sign.
     */
    public void addSign(Location location) {
        try (var conn = conn(); var stmt = conn.prepareStatement(
            "INSERT INTO permission_signs (world, x, y, z) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setDouble(2, location.getX());
            stmt.setDouble(3, location.getY());
            stmt.setDouble(4, location.getZ());
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.SEVERE, "Failed to save sign to the database.", exception);
        }
    }

    /**
     * Saves the given sign to the database.
     *
     * @param sign The sign to save.
     */
    public void saveSign(PermissionSign sign) {
        deleteSign(sign);
        addSign(sign.location());
    }

    /**
     * Gets the sign at the given location.
     *
     * @param location The location of the sign.
     * @return The sign at the given location.
     */
    public PermissionSign byLocation(Location location) {
        return allSigns().stream().filter(sign -> sign.location().equals(location)).findFirst().orElse(null);
    }

    /**
     * Gets all signs from the database.
     *
     * @return All signs from the database.
     */
    public Set<PermissionSign> allSigns() {
        var signs = new HashSet<PermissionSign>();

        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT * FROM permission_signs;")) {

            var rs = stmt.executeQuery();

            while (rs.next()) {
                var id = rs.getInt("id");
                var world = Bukkit.getWorld(rs.getString("world"));
                var x = rs.getDouble("x");
                var y = rs.getDouble("y");
                var z = rs.getDouble("z");

                var location = new Location(world, x, y, z);
                signs.add(new PermissionSign(id, location));
            }
            return signs;
        } catch (SQLException exception) {
            logger().log(Level.SEVERE, "Failed to get all signs from the database.", exception);
        }

        return signs;
    }
}
