package fyi.tiko.perms.sign.listener;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.sign.PermissionSign;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Responsible for handling the destruction of {@link PermissionSign}s.
 *
 * @author tiko
 */
public class SignBreakListener implements Listener {

    private final PermissionPlugin plugin;

    /**
     * Creates a new sign break listener from the given plugin.
     *
     * @param plugin The plugin to create the sign break listener from.
     */
    public SignBreakListener(PermissionPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles the sign break event if a {@link PermissionSign} was found at that location .
     *
     * @param event The event to handle.
     */
    @EventHandler
    public void handleSignBreak(BlockBreakEvent event) {
        var player = event.getPlayer();
        var breakedBlock = event.getBlock();
        var signs = plugin.signs();

        if (!player.hasPermission("perms.sign.break")) {
            return;
        }

        if (!(breakedBlock.getState() instanceof Sign sign)) {
            return;
        }

        var signLocation = sign.getLocation();

        if (signs.stream().noneMatch(permissionSign -> permissionSign.location().equals(signLocation))) {
            return;
        }

        plugin.runAsync(() -> {
            var foundSign = signs.stream()
                .filter(permSign -> permSign.location().equals(signLocation))
                .findFirst()
                .orElseThrow();

            plugin.signRepository().deleteSign(foundSign);
            signs.removeIf(permissionSign -> permissionSign.location().equals(signLocation));
        });
    }
}
