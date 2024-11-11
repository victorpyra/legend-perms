package fyi.tiko.perms.utils;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.user.UserPermissibleBase;
import fyi.tiko.perms.user.permission.PermissionUser;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Responsible for the actions that are executed when the plugin is in the process of loading. This includes reloading the plugin and injecting the
 * {@link UserPermissibleBase} to the player object.
 *
 * @author tiko
 */
public class LoadingActions {

    /**
     * Private constructor to hide the implicit public one.
     */
    private LoadingActions() {

    }

    /**
     * In case of a reload we have to inject the {@link UserPermissibleBase} to the player object again.
     *
     * @param plugin The plugin that is reloading.
     */
    public static void reload(PermissionPlugin plugin) {
        // Inject the permissible base in case of a reload
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            var user = PermissionUser.of(player);
            user.apply(player);

            try {
                injectPermissibleBase(user);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                plugin.getLogger().severe("Could not inject permissible base for " + player.getName());
            }
        });
    }

    /**
     * Injects the {@link UserPermissibleBase} to the player object of the given {@link PermissionUser}.
     *
     * @param user The user to inject the permissible base to.
     */
    public static void injectPermissibleBase(PermissionUser user) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        var craftHumanEntity = Class.forName("org.bukkit.craftbukkit." + BukkitServer.SERVER_VERSION + ".entity.CraftHumanEntity");
        var field = craftHumanEntity.getDeclaredField("perm");

        field.setAccessible(true);
        field.set(user.apply(), new UserPermissibleBase(user));
        field.setAccessible(false);
    }

    /**
     * Starts the scoreboard timer that updates the scoreboard and permission signs every 5 seconds
     *
     * @param plugin The plugin that is starting the timer.
     */
    public static void startUpdateTask(PermissionPlugin plugin) {
        // Start timer to update the scoreboard & signs
        // (you could leave this out, but I don't want to create an event if the group updates, so I just update it every 5 seconds)
        // for development purposes this should be enough - in a production environment you should use events
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> plugin.getServer().getOnlinePlayers().forEach(player -> {
            plugin.userScoreboardService().sendScoreboard(player);
            updateSigns(plugin, player);
        }), 20, 100);
    }

    /**
     * Updates the permission signs for the given {@link Player}.
     *
     * @param plugin The plugin that is updating the signs.
     * @param player The player to update the signs for.
     */
    private static void updateSigns(PermissionPlugin plugin, Player player) {
        var copiedSigns = new HashSet<>(plugin.signs());

        // To avoid concurrent modification exceptions we copy the signs and iterate over the copy
        copiedSigns.forEach(permissionSign -> {
            var bukkitSign = permissionSign.location().getWorld().getBlockAt(permissionSign.location()).getState();

            if (!(bukkitSign instanceof Sign sign)) {
                return;
            }

            var user = PermissionUser.of(player);
            var group = user.highestPermissionGroup();

            player.sendSignChange(sign.getLocation(), new String[]{
                "§3Perms-Sign",
                "§fName: §7" + player.getName(),
                "§fGroup: §7" + (group != null ? group.name() : "§cNone"),
                "§fPrefix: §f" + (group != null ? group.prefix() : "§cNone")
            });
        });
    }

    /**
     * Updates the given {@link PermissionGroup} in the registered {@link Set} of {@link PermissionGroup}s.
     *
     * @param permissionGroup The group to update.
     */
    public static void updateGroup(PermissionPlugin plugin, PermissionGroup permissionGroup) {
        var groups = plugin.groups();

        groups.removeIf(group -> group.name().equalsIgnoreCase(permissionGroup.name()));
        groups.add(permissionGroup);

        // We also have to update the groups the user has stored so the permissions are updated
        PermissionUser.permissionUsers().forEach((uuid, user) -> {
            var userGroups = user.groups();
            if (userGroups.containsKey(permissionGroup)) {
                var until = userGroups.get(permissionGroup);
                userGroups.put(permissionGroup, until);

                plugin.getLogger().log(Level.INFO, "Updated group {0} for user {1} with until {2}",
                    new Object[]{permissionGroup.name(), user.apply().getName(), until}
                );
            }
        });

        // Force database update
        plugin.runAsync(() -> plugin.groupRepository().saveGroup(permissionGroup));
    }

}
