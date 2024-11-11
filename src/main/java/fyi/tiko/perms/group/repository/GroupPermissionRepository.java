package fyi.tiko.perms.group.repository;

import fyi.tiko.perms.database.repository.PermissionRepository;
import fyi.tiko.perms.database.DatabaseInteraction;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.user.permission.PermissionUser;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * The repository for database operations on the group tables.
 *
 * @author tiko
 */
public class GroupPermissionRepository extends DatabaseInteraction {

    private final PermissionRepository permissionRepository;
    private final Set<PermissionGroup> cachedGroups;

    /**
     * Constructs a new {@link DatabaseInteraction} with the given {@link Logger} and {@link DataSource}.
     *
     * @param logger the logger of the plugin using this holder
     * @param source the data source to use
     */
    public GroupPermissionRepository(Logger logger, DataSource source, Set<PermissionGroup> cachedGroups) {
        super(logger, source);

        this.cachedGroups = cachedGroups;
        permissionRepository = new PermissionRepository(logger, source);
    }

    /**
     * Checks if the given group exists in the database.
     *
     * @param groupName the group to check
     * @return true if the group exists, false otherwise
     */
    public boolean exists(String groupName) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT name FROM perm_groups WHERE name=?")) {
            stmt.setString(1, groupName);
            var rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to check if group exists", exception);
        }
        return false;
    }

    /**
     * Saves the given group to the database.
     *
     * @param group the group to save
     */
    public void saveGroup(PermissionGroup group) {
        if (!exists(group.name())) {
            return;
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("UPDATE group_metadata SET prefix=?, suffix=?, weight=? WHERE name=?;")) {
            stmt.setString(1, group.prefix());
            stmt.setString(2, group.suffix());
            stmt.setInt(3, group.weight());
            stmt.setString(4, group.name());

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to save group", exception);
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("UPDATE perm_groups SET default_group=? WHERE name=?;")) {
            stmt.setBoolean(1, group.isDefault());
            stmt.setString(2, group.name());

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to save group", exception);
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM group_permissions WHERE name=?;")) {
            stmt.setString(1, group.name());

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to save group", exception);
        }

        if (group.permissions() != null) {
            group.permissions().forEach(perm -> addPermission(group.name(), perm));
        }
    }

    /**
     * @return a set of all groups in the database
     */
    public Set<PermissionGroup> groups() {
        var groups = new HashSet<PermissionGroup>();

        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT * FROM perm_groups;")) {
            var rs = stmt.executeQuery();

            while (rs.next()) {
                groups.add(byName(rs.getString("name")));
            }

            return groups;
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get groups", exception);
        }

        return Collections.emptySet();
    }

    /**
     * Adds the given group to the database.
     *
     * @param name the name of the group to add
     */
    public void addGroup(String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement("INSERT INTO perm_groups(name) VALUES (?);")) {
            stmt.setString(1, name);

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to add group", exception);
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("INSERT INTO group_metadata(name, prefix, suffix, weight) VALUES (?, ?, ?, ?);")) {
            stmt.setString(1, name);
            stmt.setString(2, "");
            stmt.setString(3, "");
            stmt.setInt(4, 0);

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to add group", exception);
        }
    }

    /**
     * Removes the given group from the database.
     *
     * @param name the name of the group to remove
     */
    public void removeGroup(String name) {
        cachedGroups.removeIf(group -> group.name().equalsIgnoreCase(name));

        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM player_groups WHERE group_name=?;")) {
            stmt.setString(1, name);

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to remove group from players", exception);
        }

        // Remove group from all users
        PermissionUser.permissionUsers().values()
            .stream()
            .filter(user -> user.isInGroup(name))
            .forEach(user -> user.removeGroup(name));

        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM group_permissions WHERE name=?;")) {
            stmt.setString(1, name);

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to remove group permissions", exception);
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM group_metadata WHERE name=?;")) {
            stmt.setString(1, name);

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to remove group metadata", exception);
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM perm_groups WHERE name=?;")) {
            stmt.setString(1, name);

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to remove group", exception);
        }
    }

    /**
     * Adds the given permission to the given group.
     *
     * @param groupName  The name of the group.
     * @param permission The permission to add.
     */
    public void addPermission(String groupName, String permission) {
        permissionRepository.addPermission(permission);

        if (!exists(groupName)) {
            return;
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("INSERT INTO group_permissions(name, permission) VALUES (?, ?);")) {
            stmt.setString(1, groupName);
            stmt.setString(2, permission);

            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to add permission to group", exception);
        }
    }

    /**
     * Retrieves the group with the given name from the database.
     *
     * @param name The name of the group.
     * @return The group with the given name.
     */
    public PermissionGroup byName(String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT * FROM perm_groups WHERE name=?;")) {
            stmt.setString(1, name);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return new PermissionGroup(
                    rs.getString("name"),
                    permissions(name),
                    prefix(name),
                    suffix(name),
                    weight(name),
                    rs.getBoolean("default_group")
                );
            }
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get group by name", exception);
        }
        return null;
    }

    /**
     * Retrieves the weight of the group with the given name from the database.
     *
     * @param name The name of the group.
     * @return The weight of the group.
     */
    public int weight(String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT weight FROM group_metadata WHERE name=?;")) {
            stmt.setString(1, name);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("weight");
            }
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get group weight", exception);
        }
        return 0;
    }

    /**
     * Retrieves the prefix of the group with the given name from the database.
     *
     * @param name The name of the group.
     * @return The prefix of the group.
     */
    public String prefix(String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT prefix FROM group_metadata WHERE name=?;")) {
            stmt.setString(1, name);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("prefix");
            }
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get group prefix", exception);
        }
        return null;
    }

    /**
     * Retrieves the suffix of the group with the given name from the database.
     *
     * @param name The name of the group.
     * @return The suffix of the group.
     */
    public String suffix(String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT suffix FROM group_metadata WHERE name=?;")) {
            stmt.setString(1, name);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("suffix");
            }
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get group suffix", exception);
        }
        return null;
    }

    /**
     * Retrieves the permissions of the group with the given name from the database.
     *
     * @param name The name of the group.
     * @return The permissions of the group.
     */
    public Set<String> permissions(String name) {
        var permissions = new HashSet<String>();

        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT permission FROM group_permissions WHERE name=?;")) {
            stmt.setString(1, name);

            var rs = stmt.executeQuery();

            while (rs.next()) {
                permissions.add(rs.getString("permission"));
            }

            return permissions;
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get group permissions", exception);
        }

        return Collections.emptySet();
    }
}
