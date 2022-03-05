package cat.nyaa.mailer.abstracts;

import cat.nyaa.mailer.player.exceptions.PlayerNotFoundException;
import cat.nyaa.mailer.player.exceptions.SelfMailException;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SubCommand<P extends JavaPlugin> {

    protected final P plugin;
    protected Player sender;
    protected String[] args;

    public SubCommand(P plugin, Player sender, String[] args) {
        this.plugin = plugin;
        this.sender = sender;
        this.args = args;
    }


    abstract public void execute();


    public P getPlugin() {
        return plugin;
    }

    public void validateReceiver(Player sender, OfflinePlayer receiver) throws PlayerNotFoundException, SelfMailException {
        if (!playerExists(receiver)) {
            throw new PlayerNotFoundException();
        }
        if (receiver.getUniqueId().equals(sender.getUniqueId())) {
            throw new SelfMailException();
        }
    }

    public boolean playerExists(OfflinePlayer player) {
        return player != null;
    }
}
