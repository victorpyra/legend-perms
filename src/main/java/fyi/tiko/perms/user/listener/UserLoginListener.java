package fyi.tiko.perms.user.listener;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.user.UserPermissibleBase;
import fyi.tiko.perms.user.permission.PermissionUser;
import fyi.tiko.perms.utils.LoadingActions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

/**
 * Listens for the {@link PlayerLoginEvent} and injects the {@link UserPermissibleBase} to the player object.
 *
 * @author tiko
 */
public class UserLoginListener implements Listener {

    private final PermissionPlugin plugin;

    /**
     * Constructs a new {@link UserLoginListener}.
     *
     * @param plugin The plugin to register the listener to.
     */
    public UserLoginListener(PermissionPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Injects the permissible base that we customized to the player object of the given {@link PermissionUser}.
     *
     * @param event The event that was fired.
     */
    @EventHandler
    public void handleUserLogin(PlayerLoginEvent event) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        var player = event.getPlayer();
        var user = PermissionUser.of(player);

        // Set the player object to the user
        user.apply(player);

        // Check if the user is allowed to log in, otherwise nothing happens
        if (event.getResult() == Result.ALLOWED) {
            // Inject the permissible base to overwrite permission checks
            LoadingActions.injectPermissibleBase(user);
        }

        // Add default groups
        plugin.groups().forEach(group -> {
            if (!group.isDefault()) {
                return;
            }

            if (user.isInGroup(group.name())) {
                return;
            }

            user.addGroup(group, -1);
        });
    }
}
