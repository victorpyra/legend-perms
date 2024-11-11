package fyi.tiko.perms.commands.sub.user;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.commands.sub.SubCommand;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.group.repository.GroupPermissionRepository;
import fyi.tiko.perms.user.permission.PermissionUser;
import fyi.tiko.perms.user.repository.UserRepository;
import fyi.tiko.perms.utils.BukkitServer;
import fyi.tiko.perms.utils.Translators;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub command that is responsible for managing users.
 *
 * @author tiko
 */
public class UserCommand extends SubCommand {

    private final GroupPermissionRepository groupRepository;
    private final UserRepository userRepository;
    private final PermissionPlugin plugin;

    /**
     * Constructs a new {@link UserCommand} with the given {@link PermissionPlugin}.
     *
     * @param plugin The {@link PermissionPlugin} to construct the {@link UserCommand} from.
     */
    public UserCommand(PermissionPlugin plugin) {
        this.plugin = plugin;
        groupRepository = plugin.groupRepository();
        userRepository = plugin.userRepository();
    }

    /**
     * Executes the sub command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments of the command.
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        var translator = plugin.userTranslator();

        plugin.runAsync(() -> {
            switch (args.length) {
                case 2 -> {
                    if (args[1].equalsIgnoreCase("info")) {
                        var name = args[0];
                        var uuid = userRepository.byName(name);

                        if (uuid == null) {
                            translator.sendTranslatedMessage(sender, "commands.user.no-user");
                            return;
                        }

                        sendUserInfo(sender, uuid);

                    } else {
                        sendHelp(sender);
                    }
                }
                case 3 -> {
                    switch (args[1].toLowerCase()) {
                        case "add" -> {
                            var name = args[0];
                            var uuid = userRepository.byName(name);
                            var permission = args[2];

                            if (uuid == null) {
                                translator.sendTranslatedMessage(sender, "commands.user.no-user");
                                return;
                            }

                            if (userRepository.hasPermission(uuid, permission)) {
                                translator.sendTranslatedMessage(sender, "commands.user.already-has-permission", name,
                                    permission);
                                return;
                            }

                            userRepository.addPermission(uuid, permission);
                            translator.sendTranslatedMessage(sender, "commands.user.added-permission", permission,
                                name);

                            onlineAction(uuid, () -> {
                                var user = PermissionUser.of(uuid);
                                user.addPermission(permission);
                            });
                        }

                        case "remove" -> {
                            var name = args[0];
                            var uuid = userRepository.byName(name);
                            var permission = args[2];

                            if (uuid == null) {
                                translator.sendTranslatedMessage(sender, "commands.user.no-user");
                                return;
                            }

                            if (!userRepository.hasPermission(uuid, permission)) {
                                translator.sendTranslatedMessage(sender, "commands.user.does-not-have-permission", name,
                                    permission);
                                return;
                            }

                            userRepository.removePermission(uuid, permission);
                            translator.sendTranslatedMessage(sender, "commands.user.removed-permission", permission,
                                name);

                            onlineAction(uuid, () -> {
                                var user = PermissionUser.of(uuid);
                                user.removePermission(permission);
                            });
                        }

                        default -> sendHelp(sender);
                    }
                }
                case 4 -> {
                    if (args[1].equalsIgnoreCase("group")) {
                        switch (args[2].toLowerCase()) {
                            case "add" -> {
                                var name = args[0];
                                var uuid = userRepository.byName(name);
                                var groupName = args[3];

                                if (uuid == null) {
                                    translator.sendTranslatedMessage(sender, "commands.user.no-user");
                                    return;
                                }

                                var group = groupRepository.byName(groupName);

                                if (group == null) {
                                    translator.sendTranslatedMessage(sender, "commands.user.not-existing-group",
                                        groupName);
                                    return;
                                }

                                if (userRepository.isInGroup(uuid, group.name())) {
                                    translator.sendTranslatedMessage(sender, "commands.user.already-in-group", name,
                                        group.name());
                                    return;
                                }

                                // permanent group, therefore -1
                                userRepository.addGroup(uuid, group.name(), -1);
                                translator.sendTranslatedMessage(sender, "commands.user.added-group", group.name(),
                                    name);

                                onlineAction(uuid, () -> {
                                    var user = PermissionUser.of(uuid);
                                    user.addGroup(group, -1);
                                });
                            }

                            case "remove" -> {
                                var name = args[0];
                                var uuid = userRepository.byName(name);
                                var groupName = args[3];

                                if (uuid == null) {
                                    translator.sendTranslatedMessage(sender, "commands.user.no-user");
                                    return;
                                }

                                var group = groupRepository.byName(groupName);

                                if (group == null) {
                                    translator.sendTranslatedMessage(sender, "commands.user.not-existing-group",
                                        groupName);
                                    return;
                                }

                                if (!userRepository.isInGroup(uuid, group.name())) {
                                    translator.sendTranslatedMessage(sender, "commands.user.not-in-group", name,
                                        group.name());
                                    return;
                                }

                                userRepository.removeGroup(uuid, group.name());
                                translator.sendTranslatedMessage(sender, "commands.user.removed-group", group.name(),
                                    name);

                                onlineAction(uuid, () -> {
                                    var user = PermissionUser.of(uuid);
                                    user.removeGroup(group);
                                });
                            }

                            default -> sendHelp(sender);
                        }
                    } else {
                        sendHelp(sender);
                    }
                }
                default -> {
                    if (args.length < 6 || !args[1].equalsIgnoreCase("group") || !args[2].equalsIgnoreCase("add")) {
                        sendHelp(sender);
                        return;
                    }

                    var name = args[0];
                    var uuid = userRepository.byName(name);
                    var groupName = args[3];

                    if (uuid == null) {
                        translator.sendTranslatedMessage(sender, "commands.user.no-user");
                        return;
                    }

                    var group = groupRepository.byName(groupName);

                    if (group == null) {
                        translator.sendTranslatedMessage(sender, "commands.user.not-existing-group", groupName);
                        return;
                    }

                    if (userRepository.isInGroup(uuid, group.name())) {
                        translator.sendTranslatedMessage(sender, "commands.user.already-in-group", name, group.name());
                        return;
                    }

                    var builder = new StringBuilder();

                    for (int i = 4; i < args.length; i++) {
                        builder.append(args[i]).append(" ");
                    }

                    var duration = builder.toString().trim();

                    if (!Translators.isCorrectDurationFormat(duration)) {
                        translator.sendTranslatedMessage(sender, "commands.user.invalid-duration-format", duration);
                        return;
                    }

                    var durationSeconds = Translators.translateDurationSeconds(duration);
                    var until = System.currentTimeMillis() + (durationSeconds * 1000);
                    userRepository.addGroup(uuid, group.name(), until);

                    translator.sendTranslatedMessage(sender, "commands.user.added-group", group.name(), name);

                    onlineAction(uuid, () -> {
                        var user = PermissionUser.of(uuid);
                        user.addGroup(group, until);
                    });
                }
            }
        });
    }

    /**
     * Executes the given {@link Runnable} if the user is online.
     *
     * @param uuid     The uuid of the user.
     * @param runnable The action to perform.
     */
    private void onlineAction(UUID uuid, Runnable runnable) {
        if (plugin.getServer().getPlayer(uuid) != null) {
            runnable.run();
        }
    }

    /**
     * Sends the user info to the given {@link CommandSender}.
     *
     * @param sender The {@link CommandSender} to send the info to.
     * @param uuid   The uuid of the user.
     */
    private void sendUserInfo(CommandSender sender, UUID uuid) {
        var translator = plugin.userTranslator();
        var server = plugin.getServer();
        var player = server.getPlayer(uuid);

        if (player != null) {
            var user = PermissionUser.of(player);
            var highestPermissionGroup = user.highestPermissionGroup();
            var groups = user.groups();
            var permissions = user.permissions();

            translator.sendTranslatedMessage(sender, "commands.user.info",
                player.getName(),
                uuid.toString(),
                highestPermissionGroup != null ? highestPermissionGroup.name() : "§c×",
                groupInfo(groups),
                String.join("§8, §f", permissions)
            );
        } else {
            // load user from database
            var groups = userRepository.groups(uuid);
            var permissions = userRepository.permissions(uuid);
            var highestPermissionGroup = userRepository.highestPermissionGroup(uuid);

            translator.sendTranslatedMessage(sender, "commands.user.info",
                userRepository.byUuid(uuid),
                uuid.toString(),
                highestPermissionGroup != null ? highestPermissionGroup.name() : "§c×",
                groupInfo(groups),
                String.join("§8, §f", permissions)
            );
        }
    }

    /**
     * Returns the group information of the provided groups in a formatted string.
     *
     * @param groups The groups to format.
     * @return The formatted string.
     */
    private String groupInfo(Map<PermissionGroup, Long> groups) {
        return String.join("§8, §f", groups.keySet().stream().map(group -> {
            var until = groups.get(group) == -1 ? -1 : (groups.get(group) - System.currentTimeMillis()) / 1000;
            return group.name() + " §8(§f" + (until == -1 ? "permanent" : Translators.secondsToFormat(until))
                + "§8)";
        }).toList());
    }

    /**
     * Sends the help message to the given {@link CommandSender}.
     *
     * @param sender The {@link CommandSender} to send the help message to.
     */
    private void sendHelp(CommandSender sender) {
        var translator = plugin.userTranslator();
        translator.sendTranslatedMessage(sender, "commands.user.help-message");
    }

    /**
     * @return The names of the sub command.
     */
    @Override
    public String[] names() {
        return new String[]{"user"};
    }

    /**
     * @return The permission of the sub command.
     */
    @Override
    public String permission() {
        return "perms.command.user";
    }

    /**
     * Suggests the arguments for the sub command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments of the command.
     * @return The suggestions for the sub command.
     */
    @Override
    public List<String> suggest(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 1 -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            case 2 -> List.of("add", "remove", "info", "group");
            case 3 -> {
                if (args[1].equalsIgnoreCase("group")) {
                    yield List.of("add", "remove");
                }
                if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    yield BukkitServer.PERMISSIONS.stream().toList();
                }
                yield Collections.emptyList();
            }
            case 4 -> {
                if (args[1].equalsIgnoreCase("group")) {
                    yield plugin.groups().stream().map(PermissionGroup::name).toList();
                }
                yield Collections.emptyList();
            }
            default -> Collections.emptyList();
        };
    }
}
