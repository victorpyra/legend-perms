package fyi.tiko.perms.commands;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.commands.sub.SubCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main command of the plugin. Here you can manage groups and permissions.
 *
 * @author tiko
 */
public class PermissionCommand implements TabExecutor {
    private final PermissionPlugin plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public PermissionCommand(PermissionPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("permission").setExecutor(this);
    }

    /**
     * Registers a sub command to the main command.
     *
     * @param commands The sub commands to register.
     */
    public void registerSubCommand(SubCommand... commands) {
        for (var subCommand : commands) {
            for (var name : subCommand.names()) {
                subCommands.put(name.toLowerCase(), subCommand);
            }
        }
    }

    /**
     * Executes the main command with the given sub commands.
     *
     * @param sender The sender of the command.
     * @param cmd    The command that was executed.
     * @param l      The label of the command.
     * @param args   The arguments of the command.
     * @return True if the command was executed successfully.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String l, @NotNull String[] args) {
        var translator = plugin.userTranslator();

        if (args.length == 0 || !subCommands.containsKey(args[0])) {
            translator.sendTranslatedMessage(sender, "commands.perms.usage", String.join(", ", subCommands.keySet()));
            return true;
        }

        // Execute the sub command with the given arguments excluding the first one so that the sub command can start again from 0.
        var subCommand = subCommands.get(args[0].toLowerCase());

        if (!sender.hasPermission(subCommand.permission())) {
            translator.sendTranslatedMessage(sender, "no-permission");
            return true;
        }

        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));

        return false;
    }

    /**
     * Responsible for suggesting the next argument for the command.
     *
     * @param sender The sender of the command.
     * @param cmd    The command that was executed.
     * @param l      The label of the command.
     * @param args   The arguments of the command.
     * @return A list of suggestions.
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String l, @NotNull String[] args) {

        if (args.length == 0) {
            return new ArrayList<>(subCommands.keySet());
        }

        // Check if the sub command exists.
        if (!subCommands.containsKey(args[0].toLowerCase())) {
            // filtering all duplicate start characters so only the first one will be returned
            return new ArrayList<>(subCommands.keySet())
                .stream()
                .filter(s -> s.startsWith(args[0]))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }

        var subCommand = subCommands.get(args[0].toLowerCase());

        // Check if the sender has permission to execute the sub command.
        if (!sender.hasPermission(subCommand.permission())) {
            return Collections.emptyList();
        }

        // Execute the sub command with the given arguments excluding the first one so that the suggestion can start from 0.
        return subCommand.suggest(sender, Arrays.copyOfRange(args, 1, args.length));
    }
}
