package cat.nyaa.mailer.selector.manager;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.Manager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectorManager extends Manager<NyaaMailer> {


    private final ConcurrentHashMap<UUID, OfflinePlayer> selectingChest;
    private final HashSet<UUID> selectingInbox;

    public SelectorManager(NyaaMailer plugin) {
        super(plugin);
        selectingChest = new ConcurrentHashMap<>();
        selectingInbox = new HashSet<>();

    }

    public boolean isSelectingInbox(Player player) {
        return selectingInbox.contains(player.getUniqueId());
    }

    public boolean isSelectingChest(Player player) {
        return selectingChest.containsKey(player.getUniqueId());
    }

    @Nullable
    public OfflinePlayer getReceiver(Player player) {
        return selectingChest.get(player.getUniqueId());
    }

    public void cancelSelectingChest(Player player) {
        selectingChest.remove(player.getUniqueId());
    }

    public void cancelSelectingInbox(Player player) {
        selectingInbox.remove(player.getUniqueId());
    }

    /**
     * If the player is alredy selecting an inbox ignore the request. If the player is selecting a chest,
     * cancel the selection and start selecting an inbox.
     * @param sender
     */
    public void processInboxSelection(Player sender) {

        if (isSelectingInbox(sender)) {
            sender.sendMessage(plugin.getMessage("already-selecting-inbox", "&cYou are already selecting an inbox."));
            return;
        }

        if (isSelectingChest(sender)) {
            cancelSelectingChest(sender);
        }
        startSelectingInboxTimer(sender);

        int timeout = plugin.getConfig().getInt("chest.timeout", 10);
        sender.sendMessage(plugin.getMessage("chest-cooldown-notify", "You have %time% seconds left to select(right-click) a chest to use as inbox").replace("%time%", String.valueOf(timeout)));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (selectingInbox.remove(sender.getUniqueId())) {
                    sender.sendMessage(plugin.getMessage("chest-selection-timeout", "You took to long to select a chest. Please re-execute the command."));
                }
            }
        }.runTaskLater(plugin, timeout * 20L);


    }

    /**
     * If the player is alredy selecting a chest ignore the request. If the player is selecting an inbox, cancel the selection and start selecting a chest.
     * @param sender the player who is selecting a chest
     * @param receiver the player who is receiving the mail
     */
    public void processChestSelection(Player sender, OfflinePlayer receiver) {

        if (isSelectingChest(sender)) {
            sender.sendMessage(plugin.getMessage("already-selecting-chest", "&cYou are already selecting a chest."));
            return;
        }
        if (isSelectingInbox(sender)) {
            cancelSelectingInbox(sender);
        }

        startSelectingChestTimer(sender, receiver);

        int timeout = plugin.getConfig().getInt("inbox.timeout", 10);
        sender.sendMessage(plugin.getMessage("inbox-cooldown-notify", "You have %time% seconds left to select(right-click) a chest to use as inbox").replace("%time%", String.valueOf(timeout)));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (selectingChest.remove(sender.getUniqueId()) != null) {
                    sender.sendMessage(plugin.getMessage("inbox-selection-timeout", "You took to long to select a chest. Please re-execute the command."));
                }
            }
        }.runTaskLater(plugin, timeout * 20L);


    }

    private void startSelectingChestTimer(Player sender, OfflinePlayer receiver) {
        selectingChest.put(sender.getUniqueId(), receiver);
    }

    private void startSelectingInboxTimer(Player sender) {
        selectingInbox.add(sender.getUniqueId());

    }

}
