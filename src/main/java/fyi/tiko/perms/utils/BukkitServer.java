package fyi.tiko.perms.utils;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;

/**
 * Contains informationen that isn't accessible from the bukkit api directly.
 *
 * @author tiko
 */
public class BukkitServer {

    /**
     * Private constructor to hide the implicit public one.
     */
    private BukkitServer() {
    }

    /**
     * The version of the server in the NMS format.
     */
    public static final String SERVER_VERSION = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];

    /**
     * Cached permissions
     */
    public static final Set<String> PERMISSIONS = new HashSet<>();
}
