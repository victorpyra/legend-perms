package fyi.tiko.perms.commands.sub.sign;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.commands.sub.SubCommand;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The sign command is used to set and remove permission signs.
 *
 * @author tiko
 */
public class SignCommand extends SubCommand {

    private final PermissionPlugin plugin;

    /**
     * Constructs a new sign command.
     *
     * @param plugin The plugin instance.
     */
    public SignCommand(PermissionPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the sign command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments of the command.
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        var translator = plugin.userTranslator();

        if (!(sender instanceof Player player)) {
            translator.sendTranslatedMessage(sender, "commands.sign.player-only");
            return;
        }

        var sign = player.getTargetBlockExact(5);
        var signs = plugin.signs();

        if (sign == null || !Tag.SIGNS.isTagged(sign.getType())) {
            translator.sendTranslatedMessage(sender, "commands.sign.look-at-sign");
            return;
        }

        plugin.runAsync(() -> {
            switch (args[0].toLowerCase()) {
                case "set" -> {

                    if (signs.stream().anyMatch(permissionSign -> permissionSign.location().equals(sign.getLocation()))) {
                        translator.sendTranslatedMessage(sender, "commands.sign.already-set");
                        return;
                    }

                    plugin.signRepository().addSign(sign.getLocation());
                    var permissionSign = plugin.signRepository().byLocation(sign.getLocation());

                    signs.add(permissionSign);
                    translator.sendTranslatedMessage(sender, "commands.sign.set-sign");
                }

                case "remove" -> {
                    if (signs.stream().noneMatch(permissionSign -> permissionSign.location().equals(sign.getLocation()))) {
                        translator.sendTranslatedMessage(sender, "commands.sign.no-sign-found");
                        return;
                    }

                    var permSign = signs.stream().filter(permissionSign -> permissionSign.location().equals(sign.getLocation())).findFirst()
                        .orElseThrow();

                    plugin.signRepository().deleteSign(permSign);
                    signs.remove(permSign);

                    plugin.getServer().getScheduler().runTask(plugin,
                        () -> permSign.location().getWorld().getBlockAt(permSign.location()).setType(Material.AIR)
                    );
                    translator.sendTranslatedMessage(sender, "commands.sign.removed-sign");
                }

                default -> translator.sendTranslatedMessage(sender, "commands.sign.usage", "set, remove");
            }
        });
    }

    /**
     * The names of the sign command.
     *
     * @return The names of the sign command.
     */
    @Override
    public String[] names() {
        return new String[]{"sign"};
    }

    /**
     * The permission required to execute the sign command.
     *
     * @return The permission required to execute the sign command.
     */
    @Override
    public String permission() {
        return "perms.command.sign";
    }

    /**
     * Suggests arguments for the sign command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments of the command.
     * @return A list of suggestions.
     */
    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("info", "set", "remove");
        } else {
            return Collections.emptyList();
        }
    }
}
