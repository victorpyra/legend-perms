package fyi.tiko.perms.user.listener;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.user.language.UserTranslator;
import fyi.tiko.perms.user.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Responsible for handling the join message and sending the scoreboard to the player.
 *
 * @author tiko
 */
public class UserJoinListener implements Listener {

    private final PermissionPlugin plugin;

    /**
     * Constructs a new {@link UserJoinListener}.
     *
     * @param plugin The plugin to register the listener to.
     */
    public UserJoinListener(PermissionPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the join message and sends the scoreboard to the player.
     *
     * @param event The event to handle.
     */
    @EventHandler
    public void handleJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var user = PermissionUser.of(player);
        var translator = new UserTranslator(plugin.messageConfig());
        var highestGroup = user.highestPermissionGroup();

        event.joinMessage(null);

        if (highestGroup != null) {
            Bukkit.broadcastMessage(translator.translatedMessage("join-message", highestGroup.prefix(), player.getName(), highestGroup.suffix()));
        }

        plugin.userScoreboardService().sendScoreboard(player);
    }
}
