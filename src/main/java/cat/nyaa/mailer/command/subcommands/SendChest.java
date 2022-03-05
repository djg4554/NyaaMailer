package cat.nyaa.mailer.command.subcommands;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.abstracts.SubCommand;
import cat.nyaa.mailer.player.exceptions.PlayerNotFoundException;
import cat.nyaa.mailer.player.exceptions.SelfMailException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class SendChest extends SubCommand<NyaaMailer> {

    public SendChest(NyaaMailer plugin, Player player, String[] args) {
        super(plugin, player, args);
    }

    public void execute() {
        if (args.length < 2) {
            sender.sendMessage("Usage: &7/mailer sendchest [player]");
            return;
        }
        OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(args[1]);
        try {
            validateReceiver(sender, receiver);
            plugin.getSelectorManager().processChestSelection(sender, receiver);
        } catch (PlayerNotFoundException e) {
            sender.sendMessage(getPlugin().getMessage("player-not-found", "&cAre you sure that this player has "));
        } catch (SelfMailException e) {
                sender.sendMessage(getPlugin().getMessage("cannot-send-to-self", "&cYou cannot send a chest to yourself."));
        }





    }
}
