package cat.nyaa.mailer.command.subcommands;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.SubCommand;
import cat.nyaa.mailer.player.exceptions.PlayerNotFoundException;
import cat.nyaa.mailer.player.exceptions.SelfMailException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Send extends SubCommand<NyaaMailer> {

    public Send(NyaaMailer plugin, Player sender, String[] args) {
        super(plugin, sender, args);
    }

    @Override
    public void execute() {
        if (args.length < 2) {
            sender.sendMessage("Usage: &7/mailer send [player]");
            return;
        }
        OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(args[1]);
        try {
            ItemStack itemToSend = sender.getInventory().getItemInMainHand();
            if (itemToSend == null || itemToSend.getType().equals(org.bukkit.Material.AIR)) {
                sender.sendMessage(plugin.getMessage("item-not-found", "You must hold an item in your hand to send it."));
                return;
            }

            //handles the mail
            validateReceiver(sender, receiver);

            getPlugin().getMailManager().processMail(sender, receiver, itemToSend);
        } catch (PlayerNotFoundException e) {
            sender.sendMessage(getPlugin().getMessage("player-not-found", "&cAre you sure that this player has "));
        } catch (SelfMailException e) {
            sender.sendMessage(getPlugin().getMessage("cannot-send-to-self", "&cYou cannot send a mail to yourself."));
        }

    }


}
