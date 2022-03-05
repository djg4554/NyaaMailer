package cat.nyaa.mailer.abstracts;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class MetaCommand<P extends JavaPlugin> implements CommandExecutor {

    protected final P plugin;

    protected MetaCommand(P plugin) {
        this.plugin = plugin;
    }

    public boolean playerHasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.isOp();
    }
}