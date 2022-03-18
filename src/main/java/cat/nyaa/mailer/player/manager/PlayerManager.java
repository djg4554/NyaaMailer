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
        double balance = plugin.getEconomy().getPlayerBalance(sender.getUniqueId());
        return switch (feeType) {
            case ITEM -> balance >= plugin.getConfig().getDouble("fee.hand", 0.0);
            case CHEST -> balance >= plugin.getConfig().getDouble("fee.chest", 0.0);
            case STORAGE -> balance >= plugin.getConfig().getDouble("fee.storage", 0.0);
        };
    }

    public void payFee(Player sender, FeeType feeType) {
        double fee = switch (feeType) {
            case ITEM -> plugin.getConfig().getDouble("fee.hand", 10.0);
            case CHEST -> plugin.getConfig().getDouble("fee.chest", 10.0);
            case STORAGE -> plugin.getConfig().getDouble("fee.storage", 10.0);
        };
        plugin.getEconomy().withdrawPlayer(sender.getUniqueId(), fee);
        plugin.getEconomy().depositSystemVault(fee);
    }

    public boolean canPayFee(OfflinePlayer player, Long items) {
        double balance = plugin.getEconomy().getPlayerBalance(player.getUniqueId());
        return balance >= plugin.getConfig().getDouble("fee.storage", 1) * items;
    }

    public void payFee(OfflinePlayer player, Long items) {
        var fee = plugin.getConfig().getDouble("fee.storage", 1.0) * items;
        plugin.getEconomy().withdrawPlayer(player.getUniqueId(), fee);
        plugin.getEconomy().depositSystemVault(fee);
    }
}
