package fyi.tiko.perms.utils;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.user.permission.PermissionUser;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The save task is responsible for saving the cached data to the database and also updating the cache.
 *
 * @author tiko
 */
public class SaveTask extends BukkitRunnable {

    private final PermissionPlugin plugin;

    /**
     * Creates a new save task from the given plugin.
     *
     * @param plugin The plugin to create the save task from.
     */
    public SaveTask(PermissionPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Saves the cached data to the database and also updates the cache.
     */
    @Override
    public void run() {
        savePermissionData();
        updateCache();
    }

    /**
     * Saves the cached data to the database.
     */
    public void savePermissionData() {
        PermissionUser.permissionUsers().forEach((uuid, user) -> {
            var groups = user.groups();
            var groupsToRemove = new HashSet<PermissionGroup>();

            // Remove expired groups
            groups.forEach((group, until) -> {
                // -1 means never expires
                if (until == -1) {
                    return;
                }

                // Remove group if expired
                if (until < System.currentTimeMillis()) {
                    groupsToRemove.add(group);
                }
            });

            // Remove expired groups
            groupsToRemove.forEach(groups::remove);

            // Save permissions & groups
            plugin.userRepository().saveUser(user);
        });

        plugin.signs().forEach(sign -> plugin.signRepository().saveSign(sign));
        plugin.groups().forEach(group -> plugin.groupRepository().saveGroup(group));
    }

    /**
     * Updates the cache with the data from the database and also removes expired groups.
     */
    public void updateCache() {
        var userRepository = plugin.userRepository();

        plugin.getServer().getOnlinePlayers().forEach(player -> {
            PermissionUser.delete(player.getUniqueId());

            var user = PermissionUser.of(player);
            user.apply(player);

            userRepository.groups(player.getUniqueId()).forEach(user::addGroup);
            userRepository.permissions(player.getUniqueId()).forEach(user::addPermission);
            user.loaded(new AtomicBoolean(true));
        });

        plugin.groups().clear();
        plugin.groups().addAll(plugin.groupRepository().groups());

        plugin.signs().clear();
        plugin.signs().addAll(plugin.signRepository().allSigns());

        BukkitServer.PERMISSIONS.clear();
        BukkitServer.PERMISSIONS.addAll(plugin.permissionRepository().permissions());
    }
}
