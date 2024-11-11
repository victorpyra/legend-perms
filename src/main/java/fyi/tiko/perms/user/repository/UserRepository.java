package fyi.tiko.perms.user.repository;

import fyi.tiko.perms.database.repository.PermissionRepository;
import fyi.tiko.perms.database.DatabaseInteraction;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.group.repository.GroupPermissionRepository;
import fyi.tiko.perms.user.permission.PermissionUser;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Used to save and load {@link PermissionUser}s from the database and also managing the permissions and groups of the user.
 *
 * @author tiko
 */
public class UserRepository extends DatabaseInteraction {

    private final PermissionRepository permissionRepository;
    private final GroupPermissionRepository groupRepository;

    /**
     * Constructs a new {@link DatabaseInteraction} with the given {@link Logger} and {@link DataSource}.
     *
     * @param logger The logger of the plugin using this holder.
     * @param source The data source to use.
     */
    public UserRepository(Logger logger, DataSource source, Set<PermissionGroup> groups) {
        super(logger, source);

        groupRepository = new GroupPermissionRepository(logger, source, groups);
        permissionRepository = new PermissionRepository(logger, source);
    }

    /**
     * Gets the name of the user by the given uuid.
     *
     * @param uuid The uuid of the user.
     * @return The name of the user.
     */
    public String byUuid(UUID uuid) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT name FROM perm_players WHERE uuid=?")) {
            stmt.setString(1, uuid.toString());
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("name");
            }
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get user by uuid", exception);
        }
        return null;
    }

    /**
     * Adds the given user to the database.
     *
     * @param uuid The uuid of the user.
     */
    public void addUser(UUID uuid, String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement("INSERT INTO perm_players(uuid, name) VALUES (?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to add user", exception);
        }
    }

    /**
     * Gets the uuid of the user by the given name.
     *
     * @param name The name of the user.
     * @return The uuid of the user.
     */
    public UUID byName(String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT uuid FROM perm_players WHERE name=?")) {
            stmt.setString(1, name);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return UUID.fromString(resultSet.getString("uuid"));
            }
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get user by name", exception);
        }
        return null;
    }

    /**
     * Gets all permissions of the user.
     *
     * @param uuid The uuid of the user.
     * @return All permissions of the user.
     */
    public Set<String> permissions(UUID uuid) {
        var permissions = new HashSet<String>();

        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT permission FROM player_permissions WHERE uuid=?")) {
            stmt.setString(1, uuid.toString());
            var resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                permissions.add(resultSet.getString("permission"));
            }
            return permissions;
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get permissions of user", exception);
        }
        return Collections.emptySet();
    }

    /**
     * Saves the given user to the database.
     *
     * @param user The user to save.
     */
    public void saveUser(PermissionUser user) {
        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM player_groups WHERE uuid=?")) {
            stmt.setString(1, user.uuid().toString());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM player_permissions WHERE uuid=?")) {
            stmt.setString(1, user.uuid().toString());
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to save user", exception);
        }

        user.groups().forEach((group, until) -> addGroup(user.uuid(), group.name(), until));
        user.permissions().forEach(permission -> addPermission(user.uuid(), permission));
    }

    /**
     * Adds the given group to the user.
     *
     * @param uuid  The uuid of the user.
     * @param group The group to add.
     * @param until The time until the group is valid.
     */
    public void addGroup(UUID uuid, String group, long until) {
        if (!groupRepository.exists(group)) {
            return;
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("INSERT INTO player_groups(uuid, group_name, group_until) VALUES (?, ?, ?); ")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, group);
            stmt.setLong(3, until);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to add group to user", exception);
        }
    }

    /**
     * Gets all groups of the user.
     *
     * @param uuid The uuid of the user.
     * @return All groups of the user.
     */
    public Map<PermissionGroup, Long> groups(UUID uuid) {
        var groups = new HashMap<PermissionGroup, Long>();

        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT *  FROM player_groups WHERE uuid=?")) {
            stmt.setString(1, uuid.toString());
            var resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                var group = groupRepository.byName(resultSet.getString("group_name"));
                var until = resultSet.getLong("group_until");

                if (group != null) {
                    groups.put(group, until);
                }
            }
            return groups;
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to get groups of user", exception);
        }
        return Collections.emptyMap();
    }

    /**
     * Checks if the user is in the given group.
     *
     * @param uuid  The uuid of the user.
     * @param group The group to check.
     * @return True if the user is in the group.
     */
    public boolean isInGroup(UUID uuid, String group) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT group_name FROM player_groups WHERE uuid=? AND group_name=?")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, group);

            var resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to check if user is in group", exception);
        }
        return false;
    }

    /**
     * Gets the highest permission group of the user.
     *
     * @param uuid The uuid of the user.
     * @return The highest permission group of the user.
     */
    public PermissionGroup highestPermissionGroup(UUID uuid) {
        return groups(uuid).keySet().stream().reduce(PermissionGroup::highestWeight).orElse(null);
    }

    /**
     * Removes the given group from the user.
     *
     * @param uuid  The uuid of the user.
     * @param group The group to remove.
     */
    public void removeGroup(UUID uuid, String group) {
        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM player_groups WHERE uuid=? AND group_name=?")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, group);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to remove group from user", exception);
        }
    }

    /**
     * Removes the given permission from the user.
     *
     * @param uuid       The uuid of the user.
     * @param permission The permission to remove.
     */
    public void removePermission(UUID uuid, String permission) {
        try (var conn = conn(); var stmt = conn.prepareStatement("DELETE FROM player_permissions WHERE uuid=? AND permission=?")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, permission);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to remove permission from user", exception);
        }
    }

    /**
     * Adds the given permission to the user.
     *
     * @param uuid       The uuid of the user.
     * @param permission The permission to add.
     */
    public void addPermission(UUID uuid, String permission) {
        permissionRepository.addPermission(permission);

        if (hasPermission(uuid, permission)) {
            return;
        }

        try (var conn = conn(); var stmt = conn.prepareStatement("INSERT INTO player_permissions(uuid, permission) VALUES (?, ?); ")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, permission);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to add permission to user", exception);
        }
    }

    /**
     * Checks if the user has the given permission.
     *
     * @param uuid       The uuid of the user.
     * @param permission The permission to check.
     * @return True if the user has the permission.
     */
    public boolean hasPermission(UUID uuid, String permission) {
        try (var conn = conn(); var stmt = conn.prepareStatement("SELECT permission FROM player_permissions WHERE uuid=? AND permission=?")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, permission);
            var rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to check if user has permission", exception);
        }
        return false;
    }

    /**
     * Updates the user in the database.
     *
     * @param uuid The uuid of the user.
     * @param name The name of the user.
     */
    public void updateUser(UUID uuid, String name) {
        try (var conn = conn(); var stmt = conn.prepareStatement(
            "INSERT INTO perm_players(uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.setString(3, name);
            stmt.executeUpdate();
        } catch (SQLException exception) {
            logger().log(Level.WARNING, "Failed to update user", exception);
        }
    }
}
