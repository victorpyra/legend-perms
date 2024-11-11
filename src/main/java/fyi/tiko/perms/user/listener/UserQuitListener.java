package fyi.tiko.perms.user.listener;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.user.permission.PermissionUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Saves the user data to the database and removes the user from the cache.
 *
 * @author tiko
 */
public class UserQuitListener implements Listener {

    private final PermissionPlugin plugin;

    /**
     * Constructs a new {@link UserQuitListener}.
     *
     * @param plugin The plugin to register the listener to.
     */
    public UserQuitListener(PermissionPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Saves the user data to the database and removes the user from the cache.
     *
     * @param event The event to handle.
     */
    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var user = PermissionUser.of(player);

        event.quitMessage(null);

        // Save the user data to the database
        plugin.runAsync(() -> {
            plugin.userRepository().saveUser(user);
            PermissionUser.delete(player.getUniqueId());
        });
    }
}
