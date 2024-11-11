package fyi.tiko.perms.user.language;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The configuration holding the translations.
 *
 * @author tiko
 */
public class TranslationConfig {

    private final FileConfiguration configuration;

    /**
     * Creates a new translation config from the given file name.
     *
     * @param plugin   The plugin to load the config from.
     * @param fileName The name of the file to load.
     */
    public TranslationConfig(JavaPlugin plugin, String fileName) {
        plugin.saveResource(fileName, false);

        File file = new File(plugin.getDataFolder(), fileName);
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * @return The configuration.
     */
    public FileConfiguration configuration() {
        return configuration;
    }
}
