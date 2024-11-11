package fyi.tiko.perms.user;

import fyi.tiko.perms.user.permission.PermissionUser;
import java.util.Arrays;
import org.bukkit.permissions.PermissibleBase;
import org.jetbrains.annotations.NotNull;

/**
 * The base class for the user permissible. This class is internally used by bukkit to check if the user has a permission.
 * We override the #hasPermission() method to check if the user has the permission in the database.
 *
 * @author tiko
 */
public class UserPermissibleBase extends PermissibleBase {
    private final PermissionUser user;

    /**
     * Creates a new user permissible base from the given user.
     * @param user The user to create the user permissible base from.
     */
    public UserPermissibleBase(@NotNull PermissionUser user) {
        super(user.apply());
        this.user = user;
    }

    /**
     * Checks if the user has the given permission.
     * @param permission The permission to check.
     * @return True if the user has the permission.
     */
    @Override
    public boolean hasPermission(@NotNull String permission) {
        // Default permission check
        if (Arrays.asList("bukkit.broadcast.user", "bukkit.broadcast").contains(permission)) {
            return true;
        }

        // Check if a user has a negative permission node
        if (user.hasPermission("-" + permission)) {
            return false;
        }

        // Check if a user has every permission node
        if (user.hasPermission("*")) {
            return true;
        }

        // Check if a user or their group has a permission node
        return user.hasPermission(permission);
    }
}
