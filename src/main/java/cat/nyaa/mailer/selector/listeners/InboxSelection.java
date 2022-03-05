package cat.nyaa.mailer.selector.listeners;

import cat.nyaa.mailer.NyaaMailer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InboxSelection implements Listener {

    private final NyaaMailer plugin;

    public InboxSelection(NyaaMailer plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRightClickChest(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!EquipmentSlot.HAND.equals(event.getHand())) {
            return;
        }
        if (!plugin.getSelectorManager().isSelectingInbox(event.getPlayer()) ) {
            return;
        }
        event.setCancelled(true);

        Block block = event.getClickedBlock();

        if (block == null || !block.getType().name().contains("CHEST") || block.getType().name().contains("ENDER")) {
            event.getPlayer().sendMessage(plugin.getMessage("not-a-chest", "&cThis is not a chest or a valid chest. Please try again."));
            return;
        }

        switch (plugin.getChestManager().canUseChest(event.getPlayer(), block)) {
            case NOT_OWNER:
                event.getPlayer().sendMessage(plugin.getMessage("not-owner", "&cYou are not the owner of this chest. Please select another chest and try again."));
                break;
            case CHEST_PROTECTED:
                event.getPlayer().sendMessage(plugin.getMessage("selected-chest-locked", "&cYou cannot use this chest. Please try again."));
                break;
            case SUCCESS:
                plugin.getSelectorManager().cancelSelectingInbox(event.getPlayer());
                plugin.getInboxManager().createOrUpdateInbox(event.getPlayer(), block);
                event.getPlayer().sendMessage(plugin.getMessage("inbox-selected", "&aYou have selected this chest as your inbox."));
                break;
        }
    }

}
