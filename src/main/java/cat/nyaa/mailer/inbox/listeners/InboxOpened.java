package cat.nyaa.mailer.inbox.listeners;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.inbox.Inbox;
import cat.nyaa.mailer.utils.Result;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class InboxOpened implements Listener {

    private final NyaaMailer plugin;

    public InboxOpened(NyaaMailer plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChestOpened(PlayerInteractEvent event) {

        if (!event.getAction().isRightClick())
            return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType().isAir())
            return;

        if (!plugin.getChestManager().isChest(block.getLocation()))
            return;

        if (!Result.SUCCESS.equals(plugin.getChestManager().canUseChest(event.getPlayer(), block))) {
            return;
        }
        Inbox inbox = plugin.getInboxManager().isInbox(event.getPlayer(), block.getLocation());
        if (inbox == null) {
            return;
        }

        event.setCancelled(true);
        plugin.getMailManager().processChest(event.getPlayer());

        new BukkitRunnable() {
            @Override
            public void run() {
                Player opener = event.getPlayer();
                opener.closeInventory();
                opener.openInventory(inbox.getChest().getInventory());
            }
        }.runTaskLater(plugin, 1);

    }





}
