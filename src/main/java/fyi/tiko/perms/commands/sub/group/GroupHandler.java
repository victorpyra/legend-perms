package fyi.tiko.perms.commands.sub.group;

import fyi.tiko.perms.PermissionPlugin;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.group.repository.GroupPermissionRepository;
import fyi.tiko.perms.user.language.UserTranslator;
import fyi.tiko.perms.utils.LoadingActions;
import org.bukkit.command.CommandSender;

/**
 * @author Tiko
 * @created 08.11.2024, 17:07
 */
public class GroupHandler {
    private final GroupPermissionRepository groupRepository;
    private final PermissionPlugin plugin;
    private final UserTranslator translator;
    
    public GroupHandler(PermissionPlugin plugin) {
        this.plugin = plugin;
        this.groupRepository = plugin.groupRepository();
        this.translator = plugin.userTranslator();
    }
    
    /**
     * Sends the help message to the sender.
     *
     * @param sender The sender of the command.
     */
    public void sendHelpMessage(CommandSender sender) {
        var translator = plugin.userTranslator();
        translator.sendTranslatedMessage(sender, "commands.group.help-message");
    }

    /**
     *
     * @param sender
     * @param name
     * @param action
     */
    public void handleGroupAdministration(CommandSender sender, String name, String action) {
        switch (action) {
            case "create" -> createGroup(sender, name);
            case "delete", "remove" -> deleteGroup(sender, name);
            case "info" -> groupInfo(sender, name);
            default -> sendHelpMessage(sender);
        }
    }

    /**
     * Handles modifications to a specified group by performing an action on it, such as adding or
     * removing permissions, updating the default status, suffix, prefix, or weight.
     * If the group does not exist, a message is sent to the sender.
     *
     * @param sender The entity (usually a player or console) that issued the command.
     * @param name   The name of the group to modify.
     * @param action The action to perform on the group. Possible values include:
     *               "add" (adds a permission),
     *               "remove" (removes a permission),
     *               "default" (sets the group as default),
     *               "suffix" (updates the suffix),
     *               "prefix" (updates the prefix),
     *               "weight" (updates the group’s weight).
     * @param value  The value associated with the action. For example, it could be the permission
     *               to add or remove, or the new suffix, prefix, or weight value.
     */
    public void handleGroupModifications(CommandSender sender, String name, String action, String value) {
        if (!groupRepository.exists(name)) {
            plugin.userTranslator().sendTranslatedMessage(sender, "commands.group.not-existing", name);
            return;
        }

        var group = groupRepository.byName(name);
        switch (action) {
            case "add" -> addPermission(sender, group, value);
            case "remove" -> removePermission(sender, group, value);
            case "default" -> updateDefaultStatus(sender, group, value);
            case "suffix" -> updateSuffix(sender, group, value);
            case "prefix" -> updatePrefix(sender, group, value);
            case "weight" -> updateWeight(sender, group, value);
            default -> sendHelpMessage(sender);
        }
    }

    /**
     * Creates a new group with the specified name. If a group with the same name already exists,
     * a message is sent to the sender informing them that the group already exists.
     * Otherwise, the group is created, and a confirmation message is sent.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on the success or failure of the group creation.
     * @param name   The name of the group to create. This name must be unique.
     */
    public void createGroup(CommandSender sender, String name) {
        if (groupRepository.exists(name)) {
            translator.sendTranslatedMessage(sender, "commands.group.already-exists", name);
        } else {
            groupRepository.addGroup(name);
            translator.sendTranslatedMessage(sender, "commands.group.created", name);
        }
    }

    /**
     * Deletes an existing group with the specified name. If the group does not exist,
     * a message is sent to the sender indicating that the group could not be found.
     * If the group exists, it is deleted, and a confirmation message is sent to the sender.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on the success or failure of the group deletion.
     * @param name   The name of the group to delete. This name must match an existing group.
     */
    public void deleteGroup(CommandSender sender, String name) {
        if (!groupRepository.exists(name)) {
            translator.sendTranslatedMessage(sender, "commands.group.not-existing", name);
        } else {
            groupRepository.removeGroup(name);
            translator.sendTranslatedMessage(sender, "commands.group.deleted", name);
        }
    }

    /**
     * Sends information about a specified group to the sender. If the group does not exist,
     * a message is sent to inform the sender. If the group exists, details such as its name,
     * default status, weight, prefix, suffix, and permissions are sent in a formatted message.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives the information or an error message if the group does not exist.
     * @param name   The name of the group to retrieve information for.
     */
    public void groupInfo(CommandSender sender, String name) {
        if (!groupRepository.exists(name)) {
            translator.sendTranslatedMessage(sender, "commands.group.not-existing", name);
        } else {
            var group = groupRepository.byName(name);
            translator.sendTranslatedMessage(sender, "commands.group.info",
                group.name(),
                group.isDefault(),
                group.weight(),
                group.prefix(),
                group.suffix(),
                String.join("§8, §f", group.permissions()));
        }
    }

    /**
     * Adds a permission to the specified group. If the group already has the permission,
     * a message is sent to the sender indicating that the group already has it.
     * Otherwise, the permission is added, the group is updated, and a confirmation message is sent.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on whether the permission was added or already existed.
     * @param group  The group to which the permission will be added.
     * @param perm   The permission to add to the group. This should be in a standard permission
     *               format (e.g., "example.permission").
     */
    public void addPermission(CommandSender sender, PermissionGroup group, String perm) {
        if (group.hasPermission(perm)) {
            translator.sendTranslatedMessage(sender, "commands.group.already-has-permission", group.name(), perm);
        } else {
            group.addPermission(perm);
            LoadingActions.updateGroup(plugin, group);
            translator.sendTranslatedMessage(sender, "commands.group.added-permission", perm, group.name());
        }
    }

    /**
     * Removes a specified permission from a group. If the group does not have the permission,
     * a message is sent to the sender indicating this. Otherwise, the permission is removed,
     * the group is updated, and a confirmation message is sent to the sender.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on whether the permission was removed or did not exist.
     * @param group  The group from which the permission will be removed.
     * @param perm   The permission to remove from the group. This should be in a standard
     *               permission format (e.g., "example.permission").
     */
    public void removePermission(CommandSender sender, PermissionGroup group, String perm) {
        if (!group.hasPermission(perm)) {
            translator.sendTranslatedMessage(sender, "commands.group.does-not-have-permission", group.name(), perm);
        } else {
            group.removePermission(perm);
            LoadingActions.updateGroup(plugin, group);
            translator.sendTranslatedMessage(sender, "commands.group.removed-permission", perm, group.name());
        }
    }

    /**
     * Updates the default status of a specified group. If the group's default status
     * already matches the specified status, a message is sent to the sender indicating this.
     * Otherwise, the default status is updated, the group is saved, and a confirmation
     * message is sent to the sender.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on whether the default status was updated or already matched.
     * @param group  The group for which the default status will be updated.
     * @param status The new default status for the group, as a string ("true" or "false").
     *               This value is parsed to a boolean to determine if the group should be default.
     */
    public void updateDefaultStatus(CommandSender sender, PermissionGroup group, String status) {
        var isDefault = Boolean.parseBoolean(status);
        if (group.isDefault() == isDefault) {
            translator.sendTranslatedMessage(sender, "commands.group.already-default-status", isDefault);
        } else {
            group.updateDefault(isDefault);
            LoadingActions.updateGroup(plugin, group);
            translator.sendTranslatedMessage(sender, "commands.group.updated-default-status", group.name(), isDefault);
        }
    }

    /**
     * Updates the suffix of a specified group. If the group's current suffix already
     * matches the specified suffix, a message is sent to the sender indicating this.
     * Otherwise, the suffix is updated, the group is saved, and a confirmation
     * message is sent to the sender.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on whether the suffix was updated or already matched.
     * @param group  The group for which the suffix will be updated.
     * @param suffix The new suffix to set for the group. This is a string value that
     *               typically represents a text label displayed alongside the group name.
     */
    public void updateSuffix(CommandSender sender, PermissionGroup group, String suffix) {
        if (group.suffix().equals(suffix)) {
            translator.sendTranslatedMessage(sender, "commands.group.already-has-suffix", group.name(), suffix);
        } else {
            group.updateSuffix(suffix);
            LoadingActions.updateGroup(plugin, group);
            translator.sendTranslatedMessage(sender, "commands.group.updated-suffix", group.name(), suffix);
        }
    }

    /**
     * Updates the prefix of a specified group. If the group's current prefix already
     * matches the specified prefix, a message is sent to the sender indicating this.
     * Otherwise, the prefix is updated, the group is saved, and a confirmation
     * message is sent to the sender.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on whether the prefix was updated or already matched.
     * @param group  The group for which the prefix will be updated.
     * @param prefix The new prefix to set for the group. This is a string value that
     *               typically represents a label or tag displayed before the group name.
     */
    public void updatePrefix(CommandSender sender, PermissionGroup group, String prefix) {
        if (group.prefix().equals(prefix)) {
            translator.sendTranslatedMessage(sender, "commands.group.already-has-prefix", group.name(), prefix);
        } else {
            group.updatePrefix(prefix);
            LoadingActions.updateGroup(plugin, group);
            translator.sendTranslatedMessage(sender, "commands.group.updated-prefix", group.name(), prefix);
        }
    }

    /**
     * Updates the weight of a specified group. If the group's current weight already
     * matches the specified weight, a message is sent to the sender indicating this.
     * If the weight is successfully updated, the group is saved and a confirmation
     * message is sent. If the provided weight is invalid (non-numeric), an error message
     * is sent to the sender.
     *
     * @param sender The entity (e.g., player or console) that issued the command.
     *               Receives feedback on whether the weight was updated or invalid.
     * @param group  The group for which the weight will be updated.
     * @param weightStr The new weight for the group, provided as a string. It is parsed
     *                  to an integer to update the group's weight.
     */
    public void updateWeight(CommandSender sender, PermissionGroup group, String weightStr) {
        try {
            var weight = Integer.parseInt(weightStr);
            if (group.weight() == weight) {
                translator.sendTranslatedMessage(sender, "commands.group.already-has-weight", group.name(), weight);
            } else {
                group.updateWeight(weight);
                LoadingActions.updateGroup(plugin, group);
                translator.sendTranslatedMessage(sender, "commands.group.updated-weight", group.name(), weight);
            }
        } catch (NumberFormatException e) {
            translator.sendTranslatedMessage(sender, "commands.group.invalid-weight", weightStr);
        }
    }
}
