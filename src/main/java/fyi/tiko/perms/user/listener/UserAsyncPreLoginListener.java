package fyi.tiko.perms.user.listener;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.user.permission.PermissionUser;
import fyi.tiko.perms.user.repository.UserRepository;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

/**
 * Loads the groups and permissions of the user asynchronously.
 *
 * @author tiko
 */
public class UserAsyncPreLoginListener implements Listener {

    private final PermissionPlugin plugin;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@link UserAsyncPreLoginListener}.
     *
     * @param plugin The plugin to register the listener to.
     */
    public UserAsyncPreLoginListener(PermissionPlugin plugin) {
        this.plugin = plugin;
        userRepository = plugin.userRepository();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Applies the permissions and groups to the {@link PermissionUser}.
     */
    @EventHandler
    public void handleAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        var uuid = event.getUniqueId();
        var user = PermissionUser.of(uuid);
        var startTime = System.currentTimeMillis();

        // Creating a new user if the user is not found in the database
        userRepository.updateUser(uuid, event.getName());

        do {
            userRepository.permissions(uuid).forEach(user::addPermission);
            userRepository.groups(uuid).forEach(user::addGroup);
            user.loaded(new AtomicBoolean(true));

            plugin.getLogger().log(Level.INFO, String.format("Loaded user %s in %dms", uuid, System.currentTimeMillis() - startTime));
        } while (!user.loaded().get());

        // Allow the user to log in
        event.setLoginResult(Result.ALLOWED);
    }
}
