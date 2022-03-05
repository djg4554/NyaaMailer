package cat.nyaa.mailer;

import cat.nyaa.mailer.inbox.listeners.InboxOpened;
import cat.nyaa.mailer.chest.manager.ChestManager;
import cat.nyaa.mailer.command.MailCommand;
import cat.nyaa.mailer.data.manager.DataManager;
import cat.nyaa.mailer.inbox.listeners.Load;
import cat.nyaa.mailer.inbox.listeners.Unload;
import cat.nyaa.mailer.inbox.managers.InboxManager;
import cat.nyaa.mailer.lang.manager.LangManager;
import cat.nyaa.mailer.mail.manager.MailManager;
import cat.nyaa.mailer.player.manager.PlayerManager;
import cat.nyaa.mailer.selector.listeners.ChestSelection;
import cat.nyaa.mailer.selector.listeners.InboxSelection;
import cat.nyaa.mailer.selector.manager.SelectorManager;
import cat.nyaa.mailer.sql.SQLiteManager;
import cat.nyaa.mailer.utils.TaskScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class NyaaMailer extends JavaPlugin {

    private DataManager dataManager;
    private LangManager langManager;
    private MailManager mailManager;
    private SQLiteManager sqliteManager;
    private PlayerManager playerManager;
    private ChestManager chestManager;
    private SelectorManager selectorManager;
    private InboxManager inboxManager;
    private TaskScheduler taskScheduler;

    private Economy econ;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        load();


    }

    public void load() {
        reloadConfig();
        loadManagers();
        registerCommands();
        loadListeners();
        loadDependencies();
    }

    private void loadListeners() {
        new ChestSelection(this);
        new InboxOpened(this);
        new InboxSelection(this);
        new Load(this);
        new Unload(this);
    }

    public void registerCommands() {
        new MailCommand(this);
    }


    private void loadManagers() {
        langManager = new LangManager(this);
        langManager.loadLanguageConfig();

        sqliteManager= new SQLiteManager(this);
        sqliteManager.loadTables();

        dataManager = new DataManager(this);

        mailManager = new MailManager(this);
        inboxManager = new InboxManager(this);

        chestManager = new ChestManager(this);
        playerManager = new PlayerManager(this);

        selectorManager = new SelectorManager(this);
        taskScheduler = new TaskScheduler(this);
        taskScheduler.startPlayerFeesTask();


    }

    private void loadDependencies() {
        if (!setupEconomy()) {
            getLogger().info("Vault not found, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("LockettePro") == null) {
            getLogger().warning("LockettePro not found, mailer will not work");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().warning("WorldGuard not found, mailer will not work");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public String getMessage(String key, String defaultMessage) {
        return color(langManager.getLanguageConfig().getString(key, defaultMessage));
    }

    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    public Component getCMessage(String key) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(langManager.getLanguageConfig().getString(key, "&c Message error for "+ key));
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public MailManager getMailManager() {
        return mailManager;
    }

    public SQLiteManager getSQLiteManager() {
        return sqliteManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public SelectorManager getSelectorManager() {
        return selectorManager;
    }

    public InboxManager getInboxManager() {
        return inboxManager;
    }


    public Economy getEconomy() {
        return econ;
    }


    public Component componentColor(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

}
