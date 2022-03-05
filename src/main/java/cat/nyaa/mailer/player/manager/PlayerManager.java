package cat.nyaa.mailer.player.manager;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.utils.FeeType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerManager {

    private final NyaaMailer plugin;

    public PlayerManager(NyaaMailer plugin) {
        this.plugin = plugin;

    }


    public void removeItemFromInventory(Player sender, ItemStack itemStack) {
        sender.getInventory().removeItem(itemStack);
    }

    public void undoRemove(Player sender, ItemStack itemStack) {
        sender.getInventory().addItem(itemStack);
    }

    public boolean canPayFee(Player sender, FeeType feeType) {
        double balance = plugin.getEconomy().getBalance(sender);
        switch (feeType) {
            case ITEM:
                return balance >= plugin.getConfig().getDouble("fee.hand", 0.0);
            case CHEST:
                return balance >= plugin.getConfig().getDouble("fee.chest", 0.0);
            case STORAGE:
                return balance >= plugin.getConfig().getDouble("fee.storage", 0.0);
            default:
                return false;
        }
    }

    public void payFee(Player sender, FeeType feeType) {
        double fee = 0.0;
        switch (feeType) {
            case ITEM:
                fee = plugin.getConfig().getDouble("fee.hand", 10.0);
                break;
            case CHEST:
                fee = plugin.getConfig().getDouble("fee.chest", 10.0);
                break;
            case STORAGE:
                fee = plugin.getConfig().getDouble("fee.storage", 10.0);
                break;
        }
        plugin.getEconomy().withdrawPlayer(sender, fee);

    }

    public boolean canPayFee(OfflinePlayer player, Long items) {
        double balance = plugin.getEconomy().getBalance(player);
        return balance >= plugin.getConfig().getDouble("fee.storage", 1) * items;
    }

    public void payFee(OfflinePlayer player, Long items) {
        plugin.getEconomy().withdrawPlayer(player, plugin.getConfig().getDouble("fee.storage", 1.0) * items);
    }
}
