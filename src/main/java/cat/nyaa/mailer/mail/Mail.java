package cat.nyaa.mailer.mail;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.sql.Date;

public class Mail {
    private final OfflinePlayer sender;
    private final OfflinePlayer receiver;
    private ItemStack item;
    private final Date date;
    private Integer qty;

    public Mail(OfflinePlayer sender, OfflinePlayer receiver, ItemStack item, Date date, Integer qty) {
        this.sender = sender;
        this.receiver = receiver;
        this.item = item;
        this.date = date;
        this.qty = qty;
    }


    public OfflinePlayer getSender() {
        return sender;
    }

    public OfflinePlayer getReceiver() {
        return receiver;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public Date getDate() {
        return date;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(int i) {
        this.qty = i;
    }

    public void setItemStack(ItemStack itemStack) {
        this.item = itemStack;
    }
}
