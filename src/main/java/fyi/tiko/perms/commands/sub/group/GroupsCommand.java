package fyi.tiko.perms.commands.sub.group;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.commands.sub.SubCommand;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.group.repository.GroupPermissionRepository;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * Sends a list of all groups to the executing instance.
 *
 * @author tiko
 */
public class GroupsCommand extends SubCommand {
    private final PermissionPlugin plugin;
    private final GroupPermissionRepository groupRepository;

    public GroupsCommand(PermissionPlugin plugin) {
        this.plugin = plugin;
        groupRepository = plugin.groupRepository();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var translator = plugin.userTranslator();

        plugin.runAsync(() -> {
            var groups = groupRepository.groups().stream().map(PermissionGroup::name).toList();
            translator.sendTranslatedMessage(sender, "commands.groups", String.join("§8, §f", groups));
        });
    }

    /**
     * @return Names that can be used to execute the sub command.
     */
    @Override
    public String[] names() {
        return new String[]{"groups"};
    }

    /**
     * @return The permission required to execute the sub command.
     */
    @Override
    public String permission() {
        return "perms.command.groups";
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
        return Collections.emptyList();
    }
}
