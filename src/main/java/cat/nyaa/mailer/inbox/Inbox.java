package cat.nyaa.mailer.inbox;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Inbox {
    private final Chest chest;
    private final Location location;

    public Inbox(Chest chest, Location location) {
        this.chest = chest;
        this.location = location;
    }


    public Location getLocation() {
        return location;
    }



    public Chest getChest() {
        return chest;
    }

    public boolean isFull() {
        return chest.getInventory().firstEmpty() == -1;
    }

    public int getNumItems() {
        return chest.getInventory().getContents().length;
    }


    public int getNextEmptySlot() {
        return chest.getInventory().firstEmpty();
    }

    @Nullable
    public ItemStack addItem(ItemStack itemStack) {
        HashMap<Integer, ItemStack> leftovers = chest.getInventory().addItem(itemStack);
        if (leftovers.isEmpty()) {
            return null;
        } else {
            return leftovers.get(0);
        }
    }
}
