package fyi.tiko.perms.commands.sub.group;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.commands.sub.SubCommand;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.group.repository.GroupPermissionRepository;
import fyi.tiko.perms.utils.BukkitServer;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * Responsible for handling the group command.
 *
 * @author tiko
 */
public class GroupCommand extends SubCommand {

    private final PermissionPlugin plugin;
    private final GroupPermissionRepository groupRepository;

    /**
     * Constructs a new group command.
     *
     * @param plugin The plugin instance.
     */
    public GroupCommand(PermissionPlugin plugin) {
        this.plugin = plugin;

        groupRepository = plugin.groupRepository();
    }

    /**
     * Executes the group command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments of the command.
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        var groupCommandHandler = new GroupHandler(plugin);

        if (args.length < 2) {
            groupCommandHandler.sendHelpMessage(sender);
            return;
        }

        var name = args[0];
        var action = args[1].toLowerCase();

        switch (args.length) {
            case 2 -> groupCommandHandler.handleGroupAdministration(sender, name, action);
            case 3 -> groupCommandHandler.handleGroupModifications(sender, name, action, args[2]);
            default -> groupCommandHandler.sendHelpMessage(sender);
        }
    }

    /**
     * @return Names that can be used to execute the sub command.
     */
    @Override
    public String[] names() {
        return new String[]{"group"};
    }

    /**
     * @return The permission required to execute the sub command.
     */
    @Override
    public String permission() {
        return "perms.command.group";
    }

    /**
     * Responsible for suggesting the next argument for the command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments of the command.
     * @return A list of suggestions.
     */
    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 0 -> groupRepository.groups().stream().map(PermissionGroup::name).toList();
            case 2 -> List.of("add", "remove", "info", "default", "suffix", "prefix", "weight", "create", "remove");
            case 3 -> switch (args[1].toLowerCase()) {
                case "add", "remove" -> BukkitServer.PERMISSIONS.stream().toList();
                case "default" -> List.of("true", "false");
                default -> Collections.emptyList();
            };
            default -> Collections.emptyList();
        };
    }
}
