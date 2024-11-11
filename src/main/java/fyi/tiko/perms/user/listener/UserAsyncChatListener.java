package fyi.tiko.perms.user.listener;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.user.permission.PermissionUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Here the chat format of the groups is applied if present. If no group is found a default chat format is used.
 *
 * @author tiko
 */
public class UserAsyncChatListener implements Listener {

    private final PermissionPlugin plugin;

    /**
     * Constructs a new {@link UserAsyncChatListener}.
     *
     * @param plugin The plugin to register the listener to.
     */
    public UserAsyncChatListener(PermissionPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Applies the chat format of the group to the player.
     *
     * @param event The event to handle.
     */
    @EventHandler
    public void handleAsyncChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        var user = PermissionUser.of(player);
        var highestPermissionGroup = user.highestPermissionGroup();

        if (highestPermissionGroup != null) {
            var format = plugin.message("chat.format")
                .replace("{prefix}", highestPermissionGroup.prefix())
                .replace("{suffix}", highestPermissionGroup.suffix())
                .replace("{player}", "%1$s")
                .replace("{message}", "%2$s");

            event.setFormat(format);
        } else {
            event.setFormat("§7%s§8: §f%s");
        }
    }
}
