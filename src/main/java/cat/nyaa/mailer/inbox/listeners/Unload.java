package cat.nyaa.mailer.inbox.listeners;

import cat.nyaa.mailer.NyaaMailer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Unload implements Listener {

    private final NyaaMailer plugin;

    public Unload(NyaaMailer plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void unload(PlayerQuitEvent event) {
        plugin.getInboxManager().unloadInbox(event.getPlayer());
    }
}