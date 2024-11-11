package fyi.tiko.perms.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * A simple sub command system.
 * This is used to create sub commands for the main command and make it easier to manage them.
 * @author tiko
 */
public abstract class SubCommand {

    /**
     * Executes the sub command with the given logic.
     *
     * @param sender The sender of the command.
     * @param args The arguments of the command.
     */
    public abstract void execute(CommandSender sender, String[] args);

    /**
     * @return Names that can be used to execute the sub command.
     */
    public abstract String[] names();

    /**
     * @return The permission required to execute the sub command.
     */
    public abstract String permission();

    /**
     * Responsible for suggesting the next argument for the command.
     *
     * @param sender The sender of the command.
     * @param args The arguments of the command.
     * @return A list of suggestions.
     */
    public abstract List<String> suggest(CommandSender sender, String[] args);
}
