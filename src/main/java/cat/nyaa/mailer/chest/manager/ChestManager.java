package cat.nyaa.mailer.chest.manager;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.utils.FeeType;
import cat.nyaa.mailer.utils.Result;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.crafter.mc.lockettepro.LocketteProAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ChestManager {

    public final NyaaMailer plugin;


    public ChestManager(NyaaMailer plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if the player can open the chest.
     *
     * @param player the player
     * @param chest  the chest to open
     * @return SUCCESS if the player can open the chest, otherwise the reason why the player cannot open the chest.
     */
    public Result canUseChest(Player player, Block chest) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(chest.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (LocketteProAPI.isLocked(chest) && !(LocketteProAPI.isOwner(chest, player) || LocketteProAPI.isUser(chest, player))) {
            return Result.NOT_OWNER;
        }

        if (!query.testState(loc, localPlayer, Flags.CHEST_ACCESS)) {

            return Result.CHEST_PROTECTED;
        }



        return Result.SUCCESS;

    }


    public void processSelectedChest(Player player, Block block) {
        if (!isChest(block.getLocation())) {
            player.sendMessage(plugin.getMessage("error-occurred", "Something went wrong. Please try again."));
            return;
        }
        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();
        if (!plugin.getPlayerManager().canPayFee(player, FeeType.CHEST)) {
            player.sendMessage(plugin.getMessage("not-enough-balance", "You don't have enough balance to open this chest."));
            plugin.getSelectorManager().cancelSelectingChest(player);
            return;
        }

        plugin.getPlayerManager().payFee(player, FeeType.CHEST);
        plugin.getMailManager().sendChest(inventory, player);

    }
    public boolean isChest(Location location) {
        return (location.getBlock().getState() instanceof Chest);
    }


}


//            if (inventory instanceof DoubleChestInventory) {
//                DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
//                // You have a double chest instance
//
//            }