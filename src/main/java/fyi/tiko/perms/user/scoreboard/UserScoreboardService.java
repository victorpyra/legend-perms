package fyi.tiko.perms.user.scoreboard;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.user.permission.PermissionUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Responsible for sending the scoreboard to the user.
 *
 * @author tiko
 */
public class UserScoreboardService {

    private final PermissionPlugin plugin;

    /**
     * Creates a new user scoreboard service from the given plugin.
     *
     * @param plugin The plugin to create the user scoreboard service from.
     */
    public UserScoreboardService(PermissionPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends the scoreboard to the given player.
     *
     * @param player The player to send the scoreboard to.
     */
    public void sendScoreboard(Player player) {
        var scoreboard = player.getScoreboard();

        if (scoreboard.equals(plugin.getServer().getScoreboardManager().getMainScoreboard())) {
            scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        }

        player.setScoreboard(scoreboard);

        for (var onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            sendTeam(onlinePlayer, scoreboard);
            sendTeam(player, onlinePlayer.getScoreboard());
            sendSidebar(player);
        }
    }

    /**
     * Sends the sidebar to the given player.
     *
     * @param player The player to send the sidebar to.
     */
    public void sendSidebar(Player player) {
        var scoreboard = player.getScoreboard();
        var user = PermissionUser.of(player);

        if (scoreboard.equals(plugin.getServer().getScoreboardManager().getMainScoreboard())) {
            scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        }

        var objective = scoreboard.getObjective("perms") == null
            ? scoreboard.registerNewObjective("perms", "dummy", "§aPermissions")
            : scoreboard.getObjective("perms");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 1; plugin.getConfig().contains("scoreboard." + i); i++) {
            var line = String.valueOf(i);

            var team = scoreboard.getTeam(line) == null
                ? scoreboard.registerNewTeam(line)
                : scoreboard.getTeam(line);

            var configPrefix = plugin.getConfig().getString("scoreboard." + line + ".prefix");
            var prefix = !configPrefix.isBlank() ? configPrefix : "";

            var configSuffix = plugin.getConfig().getString("scoreboard." + line + ".suffix");
            var suffix = !configSuffix.isEmpty() ? configSuffix : "";

            var highestGroup = user.highestPermissionGroup();

            if (highestGroup != null) {
                prefix = prefix.replace("%prefix%", highestGroup.prefix()).replace("%group%", highestGroup.name()).replace("%suffix%", suffix);
                suffix = suffix.replace("%prefix%", highestGroup.prefix()).replace("%group%", highestGroup.name()).replace("%suffix%", suffix);
            }

            var entry = plugin.getConfig().getString("scoreboard." + line + ".entry");

            team.setPrefix(prefix);
            team.setSuffix(suffix);
            team.addEntry(entry);

            objective.getScore(entry).setScore(i);
        }

    }

    /**
     * Sends the team to the given scoreboard.
     *
     * @param player     The player to create the team from.
     * @param scoreboard The scoreboard to send the team to.
     */
    private void sendTeam(Player player, Scoreboard scoreboard) {
        var user = PermissionUser.of(player);

        var highestGroup = user.highestPermissionGroup();

        if (highestGroup == null) {
            return;
        }

        var weight = highestGroup.weight();
        var prefix = highestGroup.prefix();
        var suffix = highestGroup.suffix();

        var combinedPrefix = prefix == null || prefix.isBlank()
            ? "§7"
            : prefix + " §8| ";

        var groupName = weight + combinedPrefix + highestGroup.name();

        var team = scoreboard.getTeam(groupName) == null
            ? scoreboard.registerNewTeam(groupName)
            : scoreboard.getTeam(groupName);

        team.setPrefix(combinedPrefix);
        team.setSuffix(suffix == null ? "" : suffix);
        team.setColor(ChatColor.GRAY);
        team.addEntry(player.getName());
    }
}
