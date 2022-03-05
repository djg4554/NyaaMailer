package cat.nyaa.mailer.inbox.managers;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.Manager;
import cat.nyaa.mailer.inbox.Inbox;
import cat.nyaa.mailer.chest.exceptions.InboxRemovedException;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InboxManager extends Manager<NyaaMailer> {

    private final ConcurrentHashMap<UUID, Inbox> inboxes;
    private final InboxDataManager dataManager;

    public InboxManager(NyaaMailer plugin) {
        super(plugin);
        dataManager = new InboxDataManager(plugin);
        inboxes = new ConcurrentHashMap<>();

    }

    @Nullable
    public Inbox retrievePlayerInbox(Player player) {
        return inboxes.get(player.getUniqueId());
    }

    public void createOrUpdateInbox(Player player, Block block) {
        Inbox inbox = new Inbox((Chest) block.getState(), block.getLocation().toBlockLocation());
        inboxes.put(player.getUniqueId(), inbox);
        dataManager.insertOrUpdate(player, inbox);
    }

    public void removeInbox(Player player) {
        Inbox inbox = inboxes.remove(player.getUniqueId());
        if (inbox != null) {
            dataManager.delete(player);
        }
    }

    /**
     * Load the player inbox from the database.
     * If the chunk is not loaded, it will be loaded.
     * then unload the chunk after the inbox load.
     *
     * @param player the owner of the inbox
     */
    public void loadInbox(Player player) {
        dataManager.load(player).whenComplete((inbox, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof InboxRemovedException) {
                    player.sendMessage(plugin.getMessage("error-inbox-removed", "&cAre you sure that the block is a chest?"));
                }
                return;
            }
            if (inbox == null) {
                player.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred "));
                return;
            }

            inboxes.put(player.getUniqueId(), inbox);
            if (inbox.getLocation().getChunk().isForceLoaded()) {
                inbox.getLocation().getChunk().setForceLoaded(false);
                inbox.getLocation().getChunk().unload();

            }
        });
    }


    /**
     * Unload the inbox from memory.
     * @param player the owner of the inbox
     */
    public void unloadInbox(Player player) {
        Inbox inbox = inboxes.remove(player.getUniqueId());
        if (inbox != null) {
            dataManager.unload(player, inbox);
        }
    }

    /**
     * Check if the block in the location is an inbox
     * @param player the owner of the presumed inbox
     * @param location the location of the presumed inbox
     * @return the inbox if it exists, null otherwise
     */
    public Inbox isInbox(Player player, Location location) {
        Inbox inbox = inboxes.get(player.getUniqueId());
        if (inbox == null) {
            return null;
        }
        if (inbox.getLocation().getBlockX() == location.getBlockX() && inbox.getLocation().getBlockY() == location.getBlockY() && inbox.getLocation().getBlockZ() == location.getBlockZ()) {
            return inbox;
        }

        return null;
    }
}
