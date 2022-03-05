package cat.nyaa.mailer.abstracts;

import org.bukkit.plugin.java.JavaPlugin;

public class Manager<T extends JavaPlugin> {

    protected final T plugin;

    public Manager(T plugin) {
        this.plugin = plugin;
    }
}
