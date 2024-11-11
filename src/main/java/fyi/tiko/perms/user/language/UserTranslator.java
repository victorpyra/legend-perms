package fyi.tiko.perms.user.language;

import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

/**
 * Simple, easily expandable translation system. The language has to be set in the configuration file.
 *
 * @author tiko
 */
public class UserTranslator {

    private final TranslationConfig config;
    private final Map<String, String> translations;
    private final String usedLanguage;

    /**
     * Creates a new user translator with the given configuration.
     *
     * @param config The configuration to use.
     */
    public UserTranslator(TranslationConfig config) {
        this.config = config;
        var cfg = config.configuration();

        usedLanguage = cfg.getString("used-language");
        translations = cfg.getConfigurationSection("languages").getKeys(true).stream()
            .filter(key -> !key.equals("used-language"))
            .collect(Collectors.toMap(key -> key, key -> cfg.getString("languages." + key)));
    }

    /**
     * Sends the translated message to the sender.
     *
     * @param key  The key to translate.
     * @param args The arguments to replace in the message.
     */
    public void sendTranslatedMessage(CommandSender sender, String key, Object... args) {
        var message = translatedMessage(key, args);
        sender.sendMessage(message);
    }

    /**
     * Translates the given key with the given arguments.
     *
     * @param key  The key to translate.
     * @param args The arguments to replace in the message.
     * @return The translated message.
     */
    public String translatedMessage(String key, Object... args) {
        if (key.equals("prefix")) {
            return config.configuration().getString("prefix").replace("&", "§");
        }

        var message = translations.get(usedLanguage + "." + key);

        if (message == null) {
            return "Translation for key " + key + " not found.";
        }

        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i].toString());
        }

        // Translate the message and replace color codes and line breaks in translations
        return message.replace("&", "§").replace("%n", "\n").replace("%prefix%", translatedMessage("prefix"));
    }
}
