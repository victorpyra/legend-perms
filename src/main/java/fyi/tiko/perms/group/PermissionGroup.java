package fyi.tiko.perms.group;

import java.util.Objects;
import java.util.Set;
import org.bukkit.ChatColor;

/**
 * Represents a permission group.
 *
 * @author tiko
 */
public class PermissionGroup {

    private final String name;
    private final Set<String> permissions;
    private String prefix;
    private String suffix;
    private int weight;
    private boolean isDefault;

    /**
     * Creates a new permission group with the given name.
     *
     * @param name The name of the group.
     */
    public PermissionGroup(String name, Set<String> permissions, String prefix, String suffix, int weight, boolean isDefault) {
        this.name = name;
        this.permissions = permissions;
        this.prefix = prefix;
        this.suffix = suffix;
        this.weight = weight;
        this.isDefault = isDefault;
    }

    /**
     * Checks if the group has the given permission.
     *
     * @param permission The permission to check.
     * @return True if the group has the permission.
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Adds the given permission to the group.
     *
     * @param permission The permission to add.
     */
    public void addPermission(String permission) {
        permissions.add(permission);
    }

    /**
     * Removes the given permission from the group.
     *
     * @param permission The permission to remove.
     */
    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    /**
     * Compares the weight of the two groups and returns the group with the highest weight.
     *
     * @param group The group to compare the weight with.
     * @return The group with the highest weight.
     */
    public PermissionGroup highestWeight(PermissionGroup group) {
        return weight > group.weight ? this : group;
    }

    /**
     * @return The name of the group.
     */
    public String name() {
        return name;
    }

    /**
     * @return The {@link Set} of permissions the group has.
     */
    public Set<String> permissions() {
        return permissions;
    }

    /**
     * Updates the prefix of the group.
     *
     * @param prefix The new prefix.
     */
    public void updatePrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return The prefix of the group.
     */
    public String prefix() {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    /**
     * Updates the suffix of the group.
     *
     * @param suffix The new suffix.
     */
    public void updateSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * @return The suffix of the group.
     */
    public String suffix() {
        if (suffix == null || suffix.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', suffix);
    }

    /**
     * Updates the weight of the group.
     *
     * @param weight The new weight.
     */
    public void updateWeight(int weight) {
        this.weight = weight;
    }

    /**
     * @return The weight of the group.
     */
    public int weight() {
        return weight;
    }

    /**
     * Updates the default status of the group.
     *
     * @param isDefault The new default status.
     */
    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * @return True if the group is a default group.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Compares the given object with this group.
     *
     * @param o The object to compare.
     * @return True if the object is equal to this group.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionGroup group = (PermissionGroup) o;
        return weight == group.weight && isDefault == group.isDefault && Objects.equals(name, group.name) && Objects.equals(
            permissions, group.permissions) && Objects.equals(prefix, group.prefix) && Objects.equals(suffix, group.suffix);
    }

    /**
     * @return The hash code of this group.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, permissions, prefix, suffix, weight, isDefault);
    }

    /**
     * @return The name of the group.
     */
    @Override
    public String toString() {
        return name;
    }
}
