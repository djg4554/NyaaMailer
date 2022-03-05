package cat.nyaa.mailer.inbox.listeners;

import cat.nyaa.mailer.NyaaMailer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Load implements Listener {

    private final NyaaMailer plugin;

    public Load(NyaaMailer plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLoad(PlayerJoinEvent event) {
        plugin.getInboxManager().loadInbox(event.getPlayer());
    }
}
