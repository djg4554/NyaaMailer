package cat.nyaa.mailer.mail.manager;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.inbox.Inbox;
import cat.nyaa.mailer.mail.Mail;
import cat.nyaa.mailer.utils.FeeType;
import cat.nyaa.mailer.utils.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static cat.nyaa.mailer.utils.ItemManager.encodeItem;

public class MailManager {

    private final NyaaMailer plugin;

    public MailManager(NyaaMailer plugin) {
        this.plugin = plugin;
    }

    public void processMail(Player sender, OfflinePlayer receiver, ItemStack itemToSend) {
        if (itemToSend == null || itemToSend.getType().equals(Material.AIR)) {
            sender.sendMessage(plugin.getMessage("item-not-found", "&cAre you sure that you have an item in your hand?"));
            return;
        }
        if (!plugin.getPlayerManager().canPayFee(sender, FeeType.ITEM)) {
            sender.sendMessage(plugin.getMessage("player-not-enough-balance", "&cYou don't have enough balance to send this item."));
            return;
        }

        String itemEncoded = encodeItem(itemToSend);

        plugin.getDataManager().hasAlredySentItem(itemEncoded, sender.getUniqueId(), receiver.getUniqueId()).whenComplete((hasSent, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred "));
                return;
            }
            if (hasSent) {
                sender.sendMessage(plugin.getMessage("item-already-sent", "&cYou have already sent this item to this player."));
            } else {
                sendItem(itemToSend, sender, receiver);
            }
        });
    }

    public void sendChestItem(Inventory inventory, ItemStack itemStack, Player sender, OfflinePlayer receiver) {
        plugin.getDataManager().sendItem(encodeItem(itemStack), sender.getUniqueId(), receiver.getUniqueId());
    }

    public void sendItem(ItemStack itemStack, Player sender,OfflinePlayer receiver) {
        plugin.getPlayerManager().removeItemFromInventory(sender, itemStack);

        plugin.getDataManager().sendItem(encodeItem(itemStack), sender.getUniqueId(), receiver.getUniqueId()).whenComplete(
                (aVoid, throwable) -> {
                    if (throwable != null) {
                        plugin.getPlayerManager().undoRemove(sender, itemStack);

                        sender.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred "));
                        return;
                    }
                    plugin.getPlayerManager().payFee(sender, FeeType.ITEM);
                }

        );
        new BukkitRunnable(){
            private final Player player = sender;
            private final OfflinePlayer aReceiver = receiver;
            @Override
            public void run() {
                player.sendMessage(plugin.getMessage("item-sent", "&aMail sent.").replace("%receiver%", receiver.getName()));
                if (aReceiver.isOnline()) {
                    TextComponent textComponent = Component.text(plugin.getMessage("item-received-notification", "&aYou have received an item from &e%player%&a. Get it Now").replace("%player%", player.getName()))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/mailer retrieve latest"));
                    aReceiver.getPlayer().sendMessage(textComponent);
                }
            }
        }.runTaskLater(plugin, 5);
    }

    public void purge(OfflinePlayer receiver) {
        plugin.getDataManager().purge(receiver.getUniqueId());
    }

    public CompletableFuture<Integer> checkMail(Player player) {
        return plugin.getDataManager().checkMail(player.getUniqueId());

    }

    public void processConfirm(Player player) {
        plugin.getDataManager().retrieveAllMails(player.getUniqueId()).whenComplete((items, throwable) -> {
            if (throwable != null) {
                player.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred "));
                return;
            }
            if (items.isEmpty()) {
                player.sendMessage(plugin.getMessage("no-items-to-retrieve", "&cYou have no items to retrieve."));
                return;
            }

            new BukkitRunnable() {
                final ArrayList<Mail> itemsToGive = items;
                final Iterator<Mail> iterator = itemsToGive.iterator();

                @Override
                public void run() {

                    if (player != null && player.isOnline()) {
                        if (iterator.hasNext()) {
                            Mail mail = iterator.next();
                            ItemStack itemStack = mail.getItemStack();
                            for (int j = 0; j < mail.getQty(); j++) {
                                //drop the item on the ground
//                                new BukkitRunnable(){
//                                    @Override
//                                    public void run() {
//
//                                    }
//                                }.runTask(plugin);
                                player.getWorld().dropItem(player.getLocation(), itemStack);
                            }
                            iterator.remove();
                        } else {
                            this.cancel();
                            plugin.getDataManager().deletePlayerReceivedMails(player.getUniqueId());
                        }
                    }
                }
            }.runTaskTimer(plugin, 0, 2);
        });
    }

    public void processLatest(Player player) {
        plugin.getDataManager().retrieveLatestItem(player.getUniqueId()).whenComplete((mail, throwable) -> {
            if (throwable != null) {
                player.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred "));
                return;
            }
            if (mail == null) {
                player.sendMessage(plugin.getMessage("no-items-to-retrieve", "&cYou have no items to retrieve."));
                return;
            }

            if (mail.getItemStack() == null) {
                player.sendMessage(plugin.getMessage("error-occurred", "&&cAn error occurred"));
                return;
            }

            ItemStack itemStack = mail.getItemStack();
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(plugin.getMessage("inventory-full", "&cYour inventory is full"));
                return;
            }
            player.sendMessage(plugin.getMessage("item-received", "&aYou opened the last mail"));
            player.getInventory().addItem(itemStack);
            decreaseOrDeleteMail(mail);


        });
    }

    public void processChest(Player player) {
        Inbox inbox = plugin.getInboxManager().retrievePlayerInbox(player);
        if (inbox == null) {
            player.sendMessage(plugin.getMessage("not-owning-inbox", "&cYou have not created your inbox!"));
            return;
        }

        if (!plugin.getChestManager().isChest(inbox.getLocation())) {
            player.sendMessage(plugin.getMessage("inbox-not-chest", "&cYour inbox is not a chest!"));

            return;
        }

        if (inbox.isFull()) {
            player.sendMessage(plugin.getMessage("inbox-full", "&cYour inbox is full! Please empty it before retrieving items."));
            return;
        }

        plugin.getDataManager().retrieveAllMails(player.getUniqueId()).whenComplete((mails, throwable) -> {
            if (throwable != null) {
                player.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred "));
                return;
            }
            if (mails == null) {
                player.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred "));
                return;
            }
            if (mails.isEmpty()) {
                player.sendMessage(plugin.getMessage("no-items-to-retrieve", "&cYou have no items to retrieve."));
                return;
            }
            plugin.getDataManager().deletePlayerReceivedMails(player.getUniqueId());

            addItemsToChest(player, mails, inbox);


        });


    }

    public void sendChest(Inventory inv, Player p) {
        new BukkitRunnable() {
            private final Inventory inventory = inv;
            private final Player player = p;

            @Override
            public void run() {
                OfflinePlayer offlinePlayer = plugin.getSelectorManager().getReceiver(player);
                plugin.getSelectorManager().cancelSelectingChest(player);
                if (offlinePlayer == null) {
                    player.sendMessage(plugin.getMessage("error-occurred", "Something went wrong. Please try again."));
                    return;
                }
                ItemStack[] items = inventory.getContents();
                int numberOfItems = 0;
                for (ItemStack itemStack : inventory.getContents()) {
                    if (ItemManager.isNotValid(itemStack)) {
                        continue;
                    }
                    numberOfItems++;
                    plugin.getMailManager().sendChestItem(inventory, itemStack, player, offlinePlayer);

                }
                if (numberOfItems == 0) {
                    player.sendMessage(plugin.getMessage("chest-empty", "&cYou have no items to send."));
                    return;
                }
                inventory.clear();

                player.sendMessage(plugin.getMessage("chest-sent", "You have sent %amount% items to %player%.").replace("%amount%", numberOfItems + "").replace("%player%", offlinePlayer.getName()));
                if (offlinePlayer.isOnline()) {
                    TextComponent component = Component.text(plugin.getMessage("chest-received", "You have received %amount% items from %player%. Click Here to retrieve it to your inbox chest if you have one").
                            replace("%amount%", numberOfItems + "").
                            replace("%player%", player.getName()))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/mailer retrieve chest"));
                    offlinePlayer.getPlayer().sendMessage(component);
                }
            }
        }.runTaskLater(plugin, 1);
    }


    private void decreaseOrDeleteMail(Mail mail) {
        if (mail.getQty() > 1) {
            mail.setQty(mail.getQty() - 1);
            plugin.getDataManager().updateMail(mail);
        } else {
            plugin.getDataManager().deleteMail(mail);
        }
    }

    private void addItemsToChest(Player player, ArrayList<Mail> tMails, Inbox aInbox) {
        new BukkitRunnable() {
            private final Inbox inbox = aInbox;
            private final ArrayList<Mail> mails = tMails;
            private final Iterator<Mail> iterator = mails.iterator();
            private Mail currentMail;

            @Override
            public void run() {
                if (mails.isEmpty()) {
                    cancel();
                    return;
                }

                if ((inbox.getNextEmptySlot()) == -1) {
                    player.sendMessage(plugin.getMessage("inbox-full", "&cYour inbox is full! Please empty it before retrieving items."));
                    plugin.getDataManager().deletePlayerReceivedMails(player.getUniqueId());
                    iterator.forEachRemaining(mail -> plugin.getDataManager().deleteMail(mail));
                    this.cancel();
                    return;
                }

                //there is alredy an empty slot
                if (currentMail == null || currentMail.getQty() == 0) {
                    if (iterator.hasNext()) {
                        currentMail = iterator.next();
                    } else {
                        player.sendMessage(plugin.getMessage("all-items-retrieved", "&cYou have retrieved all items!"));
                        plugin.getDataManager().deletePlayerReceivedMails(player.getUniqueId());
                        cancel();
                        return;
                    }
                }

                ItemStack itemStack = currentMail.getItemStack();

                if (itemStack == null) {
                    player.sendMessage(plugin.getMessage("error-occurred", "&cAn error occurred"));
                    plugin.getDataManager().deletePlayerReceivedMails(player.getUniqueId());
                    iterator.forEachRemaining(mail -> plugin.getDataManager().deleteMail(mail));
                    this.cancel();
                    return;
                }

                ItemStack newItemStack = inbox.addItem(itemStack);
                if (newItemStack == null) {
                    currentMail.setQty(currentMail.getQty() - 1);
                } else {
                    currentMail.setItemStack(newItemStack);
                    plugin.getDataManager().sendMail(currentMail);
                    iterator.forEachRemaining(mail -> plugin.getDataManager().sendMail(mail));
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 2);
    }


}