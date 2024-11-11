package fyi.tiko.perms;

import fyi.tiko.perms.commands.PermissionCommand;
import fyi.tiko.perms.commands.sub.group.GroupCommand;
import fyi.tiko.perms.commands.sub.group.GroupsCommand;
import fyi.tiko.perms.commands.sub.sign.SignCommand;
import fyi.tiko.perms.commands.sub.user.UserCommand;
import fyi.tiko.perms.database.DatabaseProvider;
import fyi.tiko.perms.database.DatabaseSetup;
import fyi.tiko.perms.database.repository.PermissionRepository;
import fyi.tiko.perms.group.PermissionGroup;
import fyi.tiko.perms.group.repository.GroupPermissionRepository;
import fyi.tiko.perms.sign.PermissionSign;
import fyi.tiko.perms.sign.listener.SignBreakListener;
import fyi.tiko.perms.sign.repository.SignRepository;
import fyi.tiko.perms.user.language.TranslationConfig;
import fyi.tiko.perms.user.language.UserTranslator;
import fyi.tiko.perms.user.listener.UserAsyncChatListener;
import fyi.tiko.perms.user.listener.UserAsyncPreLoginListener;
import fyi.tiko.perms.user.listener.UserJoinListener;
import fyi.tiko.perms.user.listener.UserLoginListener;
import fyi.tiko.perms.user.listener.UserQuitListener;
import fyi.tiko.perms.user.repository.UserRepository;
import fyi.tiko.perms.user.scoreboard.UserScoreboardService;
import fyi.tiko.perms.utils.LoadingActions;
import fyi.tiko.perms.utils.SaveTask;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the plugin. Here the database is initialized and the commands and listeners are registered.
 *
 * @author tiko
 */
public class PermissionPlugin extends JavaPlugin {
    private final SaveTask saveTask = new SaveTask(this);
    private final Set<PermissionSign> signs = new HashSet<>();
    private final Set<PermissionGroup> groups = new HashSet<>();
    private DatabaseProvider databaseProvider;
    private UserRepository userRepository;
    private UserTranslator userTranslator;
    private TranslationConfig messageConfig;
    private PermissionRepository permissionRepository;
    private SignRepository signRepository;
    private UserScoreboardService userScoreboardService;
    private GroupPermissionRepository groupRepository;

    @Override
    public void onEnable() {
        // Initialize the database
        initializeDatabase();

        // Registering the listeners
        initializeListener();

        // Registering the commands
        initializeCommands();

        // Initialize the scoreboard service
        userScoreboardService = new UserScoreboardService(this);

        // Run the save task every 90 seconds
        saveTask.runTaskTimerAsynchronously(this, 0, 20 * 90L);

        // In case of a reload, the cache needs to be updated
        saveTask.updateCache();

        // Message configuration
        messageConfig = new TranslationConfig(this, "translations.yml");
        userTranslator = new UserTranslator(messageConfig);

        // Loading actions are executed. Here we inject the permissible base again (in case of reloads) and start the update task.
        LoadingActions.reload(this);
        LoadingActions.startUpdateTask(this);
    }

    @Override
    public void onDisable() {
        saveTask.savePermissionData();
        saveTask.cancel();

        groups.clear();
        signs.clear();

        databaseProvider.disconnect();
        databaseProvider = null;

        userRepository = null;
        permissionRepository = null;
        groupRepository = null;
        signRepository = null;

        userScoreboardService = null;
        messageConfig = null;
    }

    /**
     * Initializes the listeners.
     */
    private void initializeListener() {
        // Responsible for the chat
        new UserAsyncChatListener(this);
        // Loads the user data from the database
        new UserAsyncPreLoginListener(this);
        // Overwrites the #hasPermission() method
        new UserLoginListener(this);
        // Sends scoreboard & tab list updates
        new UserJoinListener(this);
        // Saves the user data to the database
        new UserQuitListener(this);
        // Responsible for handling the destruction of {@link PermissionSign}s.
        new SignBreakListener(this);
    }

    /**
     * Initializes the database.
     */
    private void initializeDatabase() {
        // Saving the default config so changes made will be applied
        saveDefaultConfig();

        var logger = getLogger();

        // Creating a new instance of the database provider with the given credentials
        databaseProvider = DatabaseProvider.create(
            getConfig().getString("credentials.host"),
            getConfig().getString("credentials.user"),
            getConfig().getString("credentials.password"),
            getConfig().getString("credentials.database"),
            getConfig().getInt("credentials.port")
        );

        // Try to connect to the database
        if (!databaseProvider.connect()) {
            logger.severe("Could not connect to the database.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        var dataSource = databaseProvider.dataSource();

        // Execute the queries from the dbsetup.sql file
        DatabaseSetup.executeQueries(logger, dataSource);

        // Initialize the repositories
        permissionRepository = new PermissionRepository(logger, dataSource);
        groupRepository = new GroupPermissionRepository(logger, dataSource, groups);
        userRepository = new UserRepository(logger, dataSource, groups);
        signRepository = new SignRepository(logger, dataSource);

        // Load the groups from the database
        groups.addAll(groupRepository.groups());
        signs.addAll(signRepository.allSigns());
    }

    /**
     * Initializes the commands.
     */
    private void initializeCommands() {
        var permCommand = new PermissionCommand(this);

        permCommand.registerSubCommand(
            new UserCommand(this),
            new GroupCommand(this),
            new GroupsCommand(this),
            new SignCommand(this)
        );
    }

    /**
     * Retrieves a message from the default configuration file and automatically translates the color codes.
     *
     * @param key The key of the message.
     * @return The translated message.
     */
    public String message(String key) {
        var entry = getConfig().getString(key);

        if (entry == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', entry);
    }

    /**
     * @return the {@link UserTranslator} instance.
     */
    public UserTranslator userTranslator() {
        return userTranslator;
    }

    /**
     * @return the registered {@link Set} of {@link PermissionSign}s.
     */
    public Set<PermissionSign> signs() {
        return signs;
    }

    /**
     * @return the {@link TranslationConfig} instance.
     */
    public TranslationConfig messageConfig() {
        return messageConfig;
    }

    /**
     * @return the {@link UserScoreboardService} instance.
     */
    public UserScoreboardService userScoreboardService() {
        return userScoreboardService;
    }

    /**
     * @return the registered {@link Set} of {@link PermissionGroup}s.
     */
    public Set<PermissionGroup> groups() {
        return groups;
    }

    /**
     * Runs the given action asynchronously.
     */
    public void runAsync(Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    /**
     * @return the {@link UserRepository} instance.
     */
    public UserRepository userRepository() {
        return userRepository;
    }

    /**
     * @return the {@link PermissionRepository} instance.
     */
    public PermissionRepository permissionRepository() {
        return permissionRepository;
    }

    /**
     * @return the {@link GroupPermissionRepository} instance.
     */
    public GroupPermissionRepository groupRepository() {
        return groupRepository;
    }

    /**
     * @return the {@link SignRepository} instance.
     */
    public SignRepository signRepository() {
        return signRepository;
    }

    /**
     * @return the {@link DatabaseProvider} instance.
     */
    public DatabaseProvider databaseProvider() {
        return databaseProvider;
    }
}
