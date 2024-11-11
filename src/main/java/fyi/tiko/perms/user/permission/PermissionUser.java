package fyi.tiko.perms.user.permission;

import fyi.tiko.perms.group.PermissionGroup;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.entity.Player;

/**
 * A cache for the permission data of a user.
 *
 * @author tiko
 */
public class PermissionUser {
    private static final Map<UUID, PermissionUser> PERMISSION_USER_MAP = new HashMap<>();
    private final Set<String> permissions = new HashSet<>();
    private final Map<PermissionGroup, Long> groups = new HashMap<>();
    private final UUID uuid;

    private Player player;
    private AtomicBoolean loaded;

    /**
     * Creates a new permission user from the given uuid.
     *
     * @param uuid The uuid to create the permission user from.
     */
    private PermissionUser(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Creates a new permission user from the given uuid.
     *
     * @param uuid The uuid to create the permission user from.
     * @return The created permission user.
     */
    public static PermissionUser of(UUID uuid) {
        return PERMISSION_USER_MAP.computeIfAbsent(uuid, PermissionUser::new);
    }

    /**
     * Creates a new permission user from the given player.
     *
     * @param player The player to create the permission user from.
     * @return The created permission user.
     */
    public static PermissionUser of(Player player) {
        return of(player.getUniqueId());
    }

    /**
     * @return True if the user is loaded.
     */
    public AtomicBoolean loaded() {
        return loaded;
    }

    /**
     * Sets the loaded state of the user.
     *
     * @param loaded The loaded state of the user.
     */
    public void loaded(AtomicBoolean loaded) {
        this.loaded = loaded;
    }

    /**
     * @return The bukkit player
     */
    public Player apply() {
        return player;
    }

    /**
     * Sets the bukkit player.
     *
     * @param player The bukkit player.
     */
    public void apply(Player player) {
        this.player = player;
    }

    /**
     * @return The uuid of the user.
     */
    public UUID uuid() {
        return uuid;
    }

    /**
     * Adds the given permission to the user.
     *
     * @param permission The permission to add.
     */
    public void addPermission(String permission) {
        permissions.add(permission);
    }

    /**
     * Checks if the user has the given permission.
     *
     * @param permission The permission to check.
     * @return True if the user has the permission.
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || groups.keySet().stream().anyMatch(group -> group.hasPermission(permission));
    }

    /**
     * Removes the given permission from the user.
     *
     * @param permission The permission to remove.
     */
    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    /**
     * Checks if the user is in the given group.
     * @param name The name of the group to check.
     * @return  True if the user is in the group.
     */
    public boolean isInGroup(String name) {
        return groups.keySet().stream().anyMatch(group -> group.name().equalsIgnoreCase(name));
    }

    /**
     * Adds the given group to the user.
     *
     * @param group The group to add.
     * @param until The time until the group is valid.
     */
    public void addGroup(PermissionGroup group, long until) {
        groups.put(group, until);
    }

    /**
     * Removes the given group from the user.
     *
     * @param group The group to remove.
     */
    public void removeGroup(PermissionGroup group) {
        groups.remove(group);
    }

    /**
     * Removes the given group from the user.
     * @param name The name of the group to remove.
     */
    public void removeGroup(String name) {
        groups.keySet().stream().filter(group -> group.name().equalsIgnoreCase(name)).findFirst().ifPresent(this::removeGroup);
    }

    /**
     * @return The set of all permissions the user has.
     */
    public Set<String> permissions() {
        return permissions;
    }

    /**
     * @return The set of all permission groups the user has.
     */
    public Map<PermissionGroup, Long> groups() {
        return groups;
    }

    /**
     * @return The highest permission group the user has.
     */
    public PermissionGroup highestPermissionGroup() {
        return groups.keySet().stream().reduce(PermissionGroup::highestWeight).orElse(null);
    }

    /**
     * Deletes the user from the cache.
     */
    public static void delete(UUID uuid) {
        PERMISSION_USER_MAP.remove(uuid);
    }

    /**
     * @return The map of all permission users.
     */
    public static Map<UUID, PermissionUser> permissionUsers() {
        return PERMISSION_USER_MAP;
    }
}
